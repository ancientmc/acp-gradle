package com.ancientmc.acp.task;

import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.task.step.function.*;
import com.ancientmc.acp.task.step.io.CopyFile;
import com.ancientmc.acp.task.step.io.ExtractFile;
import com.ancientmc.acp.util.Paths;
import com.ancientmc.acp.util.Util;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;

/**
 * Main decompilation task.
 */
public abstract class Decompile extends DefaultTask {
    public static final String PHASE = "decomp";
    
    @TaskAction
    public void exec() {
        Project project = getProject();
        Logger logger = project.getLogger();

        logger.lifecycle("[acp.decomp] Beginning decompilation");

        Step splitJar = new JavaExecStep()
                .setProject(project)
                .setConfiguration("jarsplitter")
                .setMainClass("net.neoforged.jarsplitter.ConsoleTool")
                .setArgs(Arrays.asList("--input", Paths.CLIENT_JAR, "--slim", Paths.SLIM_JAR, "--extra", Paths.EXTRA_JAR, "--srg", Paths.TSRG))
                .setMessage(PHASE, "Splitting JAR");
        splitJar.exec(logger, !project.file(Paths.SLIM_JAR).exists());

        String toInject = Paths.SLIM_JAR;
        File modPatches = project.file(Paths.DIR_MODPATCHES);

        if (modPatches.exists()) {
            Step injectModPatches = new InjectBinPatches()
                    .setInput(project.file(Paths.SLIM_JAR))
                    .setOutput(project.file(Paths.MODLOADER_JAR))
                    .setPatchDirectory(project.file(Paths.DIR_MODPATCHES))
                    .setProject(project)
                    .setMessage(PHASE, "Injecting Modloader and mod library patches");
            injectModPatches.exec(logger, !project.file(Paths.MODLOADER_JAR).exists());
            toInject = Paths.MODLOADER_JAR;
        }

        Step mcinject = new JavaExecStep()
                .setProject(project)
                .setConfiguration("mcinjector")
                .setMainClass("de.oceanlabs.mcp.mcinjector.MCInjector")
                .setArgs(Arrays.asList("--in", toInject, "--out", Paths.INJECT_JAR, "--acc", Paths.DIR_INJECT + "access.txt", "--exc", Paths.DIR_INJECT + "exceptions.txt", "--blacklist", Paths.DIR_INJECT + "blacklist.txt"))
                .setMessage(PHASE, "Injecting access modifiers and exception fixes");
        mcinject.exec(logger, !project.file(Paths.INJECT_JAR).exists());

        Step deobfuscate = new JavaExecStep()
                .setProject(project)
                .setConfiguration("autorenamingtool")
                .setMainClass("net.neoforged.art.Main")
                .setArgs(Arrays.asList("--input", Paths.INJECT_JAR, "--output", Paths.MAPPED_JAR, "--map", Paths.TSRG, "--src-fix", "--strip-sigs"))
                .setMessage(PHASE, "Deobfuscating JAR");
        deobfuscate.exec(logger, !project.file(Paths.MAPPED_JAR).exists());

        Step repackageDefaults = new RepackageDefaults()
                .setTsrg(project.file(Paths.TSRG))
                .setProject(project)
                .setConfiguration("specialsource")
                .setMainClass("net.md_5.specialsource.SpecialSource")
                .setArgs()
                .setMessage(PHASE, "Repackaging defaults");
        repackageDefaults.exec(logger, !project.file(Paths.REPACKAGED_JAR).exists());

        Step decompile = new JavaExecStep()
                .setProject(project)
                .setConfiguration("fernflower")
                .setMainClass("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler")
                .setArgs(Arrays.asList("-rbr=0", "-rsy=0", "-asc=1", "-din=1", "-dgs=0", "-jvn=1", "-ind=    ", Paths.REPACKAGED_JAR, Paths.FINAL_JAR))
                .setMessage(PHASE, "Decompiling JAR");
        decompile.exec(logger, !project.file(Paths.FINAL_JAR).exists());

        Step unzip = new ExtractFile()
                .setInput(project.file(Paths.FINAL_JAR))
                .setOutput(project.file(Paths.DIR_SRC))
                .setProject(project)
                .setMessage(PHASE, "Unzipping Minecraft's source");
        unzip.exec(logger, Util.directoryCondition(project.file(Paths.DIR_SRC + "com/mojang/minecraft/"))
                || Util.directoryCondition(project.file(Paths.DIR_SRC + "net/minecraft/")));

        Step patch = new JavaExecStep()
                .setProject(project)
                .setConfiguration("diffpatch")
                .setMainClass("codechicken.diffpatch.DiffPatch")
                .setArgs(Arrays.asList("--patch", Paths.DIR_SRC, Paths.DIR_PATCHES, "--output", Paths.DIR_SRC,
                        "--reject", Paths.DIR_TEMP + "patch_rejects/"))
                .setMessage(PHASE, "Patching source files");
        patch.exec(logger, true); // condition???

        Step copyResources = new ExtractFile()
                .setProject(project)
                .setInput(project.file(Paths.EXTRA_JAR))
                .setOutput(project.file(Paths.DIR_RESOURCES))
                .setExclusions(Arrays.asList("com/jcraft/**", "paulscode/**", "META-INF/**"))
                .setMessage(PHASE, "Extracting JAR resources");
        copyResources.exec(logger, Util.directoryCondition(project.file(Paths.DIR_RESOURCES)));

        Step backupSrc = new CopyFile()
                .setProject(project)
                .setInput(project.file(Paths.DIR_SRC))
                .setOutput(project.file(Paths.DIR_VANILLA_SRC))
                .setExclusions("acp/")
                .setMessage(PHASE, "Backing up source files");
        backupSrc.exec(logger, Util.directoryCondition(project.file(Paths.DIR_VANILLA_SRC)));

        Step backupResources = new CopyFile()
                .setProject(project)
                .setInput(project.file(Paths.DIR_RESOURCES))
                .setOutput(project.file(Paths.DIR_VANILLA_RESOURCES))
                .setMessage(PHASE, "Backing up JAR resources");
        backupResources.exec(logger, Util.directoryCondition(project.file(Paths.DIR_VANILLA_RESOURCES)));

        Step vanillaCompile = new JavaCompileStep()
                .setSourceDirectory(project.file(Paths.DIR_VANILLA_SRC))
                .setClasspathCollection(project.getExtensions().getByType(SourceSetContainer.class).named("main").get().getCompileClasspath())
                .setNativesDirectory(project.file(Paths.DIR_NATIVES))
                .setOutputDirectory(project.file(Paths.DIR_VANILLA_CLASSES))
                .setMessage(PHASE, "Compiling the game");
        vanillaCompile.exec(logger, Util.directoryCondition(project.file(Paths.DIR_VANILLA_CLASSES)));

        Step buildVanillaJar = new BuildJar()
                .setClassDirectory(project.file(Paths.DIR_VANILLA_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_VANILLA_RESOURCES))
                .setOutput(project.file(Paths.VANILLA_JAR))
                .setMessage(PHASE, "Rebuilding vanilla JAR");
        buildVanillaJar.exec(logger, !project.file(Paths.VANILLA_JAR).exists());

        Step makeVanillaHashes = new MakeHashes()
                .setProject(project)
                .setClassDirectory(project.file(Paths.DIR_VANILLA_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_VANILLA_RESOURCES))
                .setOutput(project.file("build/modding/hashes/vanilla.md5"))
                .setMessage(PHASE, "Generating vanilla hashes");
        makeVanillaHashes.exec(logger, !project.file("build/modding/hashes/vanilla.md5").exists());
    }
}
