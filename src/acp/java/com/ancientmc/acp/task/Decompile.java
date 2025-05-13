package com.ancientmc.acp.task;

import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.task.step.io.*;
import com.ancientmc.acp.task.step.function.*;
import com.ancientmc.acp.util.FileUtil;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;

/**
 * Main decompilation task.
 * @author moist-mason
 */
public abstract class Decompile extends AcpTask {

    @TaskAction
    public void exec() {
        Project project = getProject();
        setLogger();

        Step splitJar = new JavaExecStep(project, logger, "Splitting the JAR")
                .setConfiguration("jarsplitter")
                .setMainClass("net.neoforged.jarsplitter.ConsoleTool")
                .setArgs(Arrays.asList("--input", Paths.CLIENT_JAR, "--slim", Paths.SLIM_JAR, "--extra", Paths.EXTRA_JAR, "--srg", Paths.TSRG))
                .setCondition(!project.file(Paths.SLIM_JAR).exists());
        splitJar.exec();

        String toInject = Paths.SLIM_JAR;
        File modPatches = project.file(Paths.DIR_MODPATCHES);

        if (modPatches.exists()) {
            Step injectModPatches = new InjectBinPatches(project, logger, "Injecting mod loader and mod library patches")
                    .setInput(project.file(Paths.SLIM_JAR))
                    .setOutput(project.file(Paths.MODLOADER_JAR))
                    .setPatchDirectory(project.file(Paths.DIR_MODPATCHES))
                    .setCondition(!project.file(Paths.MODLOADER_JAR).exists());
            injectModPatches.exec();
            toInject = Paths.MODLOADER_JAR;
        }

        Step mcinject = new JavaExecStep(project, logger, "Injecting access modifiers and exception fixes")
                .setConfiguration("mcinjector")
                .setMainClass("de.oceanlabs.mcp.mcinjector.MCInjector")
                .setArgs(Arrays.asList("--in", toInject, "--out", Paths.INJECT_JAR, "--acc", Paths.DIR_INJECT + "access.txt", "--exc", Paths.DIR_INJECT + "exceptions.txt", "--blacklist", Paths.DIR_INJECT + "blacklist.txt"))
                .setCondition(!project.file(Paths.INJECT_JAR).exists());
        mcinject.exec();

        Step deobfuscate = new JavaExecStep(project, logger, "Deobfuscating JAR")
                .setConfiguration("autorenamingtool")
                .setMainClass("net.neoforged.art.Main")
                .setArgs(Arrays.asList("--input", Paths.INJECT_JAR, "--output", Paths.MAPPED_JAR, "--map", Paths.TSRG, "--src-fix", "--strip-sigs"))
                .setCondition(!project.file(Paths.MAPPED_JAR).exists());
        deobfuscate.exec();

        Step repackageDefaults = new RepackageDefaults(project, logger, "Repackaging defaults")
                .setTsrg(project.file(Paths.TSRG))
                .setConfiguration("specialsource")
                .setMainClass("net.md_5.specialsource.SpecialSource")
                .setArgs()
                .setCondition(!project.file(Paths.REPACKAGED_JAR).exists());
        repackageDefaults.exec();

        Step decompile = new JavaExecStep(project, logger, "Decompiling JAR")
                .setConfiguration("fernflower")
                .setMainClass("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler")
                .setArgs(Arrays.asList("-rbr=0", "-rsy=0", "-asc=1", "-din=1", "-dgs=0", "-jvn=1", "-ind=    ", Paths.REPACKAGED_JAR, Paths.FINAL_JAR))
                .setCondition(!project.file(Paths.FINAL_JAR).exists());
        decompile.exec();

        Step unzip = new ExtractFile(project, logger, "Unzipping Minecraft's sources")
                .setInput(project.file(Paths.FINAL_JAR))
                .setOutput(project.file(Paths.DIR_SRC))
                .setCondition(FileUtil.directoryCondition(project.file(Paths.DIR_SRC + "com/mojang/minecraft/"))
                        || FileUtil.directoryCondition(project.file(Paths.DIR_SRC + "net/minecraft/")));
        unzip.exec();

        Step patch = new JavaExecStep(project, logger, "Patching source files")
                .setConfiguration("diffpatch")
                .setMainClass("codechicken.diffpatch.DiffPatch")
                .setArgs(Arrays.asList("--patch", Paths.DIR_SRC, Paths.DIR_PATCHES, "--output", Paths.DIR_SRC,
                        "--reject", Paths.DIR_TEMP + "patch_rejects/"))
                .setCondition(true); // condition ???
        patch.exec();

        Step extractResources = new ExtractFile(project, logger, "Extracting JAR resources")
                .setInput(project.file(Paths.EXTRA_JAR))
                .setOutput(project.file(Paths.DIR_RESOURCES))
                .setExclusions(Arrays.asList("com/jcraft/**", "paulscode/**", "META-INF/**"))
                .setCondition(FileUtil.directoryCondition(project.file(Paths.DIR_RESOURCES)));
        extractResources.exec();

        Step backupSrc = new CopyFile(project, logger, "Backing up source files")
                .setInput(project.file(Paths.DIR_SRC))
                .setOutput(project.file(Paths.DIR_VANILLA_SRC))
                .setExclusions("acp/")
                .setCondition(FileUtil.directoryCondition(project.file(Paths.DIR_VANILLA_SRC)));
        backupSrc.exec();

        Step backupResources = new CopyFile(project, logger, "Backing up JAR resources")
                .setInput(project.file(Paths.DIR_RESOURCES))
                .setOutput(project.file(Paths.DIR_VANILLA_RESOURCES))
                .setCondition(FileUtil.directoryCondition(project.file(Paths.DIR_VANILLA_RESOURCES)));
        backupResources.exec();

        Step vanillaCompile = new JavaCompileStep(project, logger, "Recompiling the game")
                .setSourceDirectory(project.file(Paths.DIR_VANILLA_SRC))
                .setClasspathCollection(project.getExtensions().getByType(SourceSetContainer.class).named("main").get().getCompileClasspath())
                .setNativesDirectory(project.file(Paths.DIR_NATIVES))
                .setOutputDirectory(project.file(Paths.DIR_VANILLA_CLASSES))
                .setCondition(FileUtil.directoryCondition(project.file(Paths.DIR_VANILLA_CLASSES)));
        vanillaCompile.exec();

        Step buildVanillaJar = new BuildJar(project, logger, "Rebuilding vanilla JAR")
                .setClassDirectory(project.file(Paths.DIR_VANILLA_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_VANILLA_RESOURCES))
                .setOutput(project.file(Paths.VANILLA_JAR))
                .setCondition(!project.file(Paths.VANILLA_JAR).exists());
        buildVanillaJar.exec();

        Step makeVanillaHashes = new MakeHashes(project, logger, "Generating vanilla hashes")
                .setClassDirectory(project.file(Paths.DIR_VANILLA_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_VANILLA_RESOURCES))
                .setOutput(project.file(Paths.VANILLA_HASH_FILE))
                .setCondition(!project.file(Paths.VANILLA_HASH_FILE).exists());
        makeVanillaHashes.exec();

        logger.write();
    }
}
