package com.ancientmc.acp.tasks;

import com.ancientmc.acp.tasks.step.*;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;

/**
 * Main decompile task. Assumes the Initialize task has already been run. Otherwise
 */
public abstract class Decompile extends DefaultTask {

    @TaskAction
    public void exec() {
        Project project = getProject();
        Logger logger = project.getLogger();

        logger.lifecycle("[acp.decomp] Beginning decompilation");

        Step splitJar = new JavaExecStep()
                .setProject(project)
                .setConfiguration("jarsplitter")
                .setMainClass("net.neoforged.jarsplitter.ConsoleTool")
                .setArgs(Arrays.asList("--input", Paths.BASE_JAR, "--slim", Paths.SLIM_JAR, "--extra", Paths.EXTRA_JAR, "--srg", Paths.TSRG))
                .setMessage("[acp.decomp] Step -> Splitting JAR...");
        splitJar.exec(logger, !project.file(Paths.SLIM_JAR).exists());

        Step injectModPatches = new Step();
        String toInject = Paths.SLIM_JAR;
        File modPatches = project.file(Paths.DIR_MODPATCHES);

        if (modPatches.exists()) {
            injectModPatches = new InjectBinPatchesStep()
                    .setInput(project.file(Paths.SLIM_JAR))
                    .setOutput(project.file(Paths.MODLOADER_JAR))
                    .setPatchDirectory(project.file(Paths.DIR_MODDED_PATCHES))
                    .setProject(project)
                    .setMessage("[acp.decomp] Step -> Injecting Modloader and mod library patches...");
            toInject = Paths.MODLOADER_JAR;
            injectModPatches.exec(logger, !project.file(Paths.MODLOADER_JAR).exists());
        }

        Step mcinject = new JavaExecStep()
                .setProject(project)
                .setConfiguration("mcinjector")
                .setMainClass("de.oceanlabs.mcp.mcinjector.MCInjector")
                .setArgs(Arrays.asList("--in", toInject, "--out", Paths.INJECT_JAR, "--acc", Paths.DIR_INJECT + "access.txt", "--exc", Paths.DIR_INJECT + "exceptions.txt", "--blacklist", Paths.DIR_INJECT + "blacklist.txt"))
                .setMessage("[acp.decomp] Step -> Injecting access modifiers and exception fixes...");
        mcinject.exec(logger, !project.file(Paths.INJECT_JAR).exists());

        Step deobfuscate = new JavaExecStep()
                .setProject(project)
                .setConfiguration("autorenamingtool")
                .setMainClass("net.neoforged.art.Main")
                .setArgs(Arrays.asList("--input", Paths.INJECT_JAR, "--output", Paths.MAPPED_JAR, "--map", Paths.TSRG, "--src-fix", "--strip-sigs"))
                .setMessage("[acp.decomp] Step -> Deobfuscating JAR...");
        deobfuscate.exec(logger, !project.file(Paths.MAPPED_JAR).exists());

        Step decompile = new JavaExecStep()
                .setProject(project)
                .setConfiguration("fernflower")
                .setMainClass("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler")
                .setArgs(Arrays.asList("-rbr=0", "-rsy=0", "-asc=1", "-din=1", "-dgs=0", "-jvn=1", "-ind=    ", Paths.MAPPED_JAR, Paths.FINAL_JAR))
                .setMessage("[acp.decomp] Step -> Decompiling JAR...");
        decompile.exec(logger, !project.file(Paths.FINAL_JAR).exists());

        Step unzip = new ExtractFileStep()
                .setInput(project.file(Paths.FINAL_JAR))
                .setOutput(project.file(Paths.DIR_SRC))
                .setProject(project)
                .setMessage("[acp.decomp] Step -> Unzipping Minecraft's source...");
        unzip.exec(logger, !project.file(Paths.DIR_SRC + "com/mojang/minecraft/").exists()
                || !project.file(Paths.DIR_SRC + "net/minecraft/").exists());

        Step patch = new JavaExecStep()
                .setProject(project)
                .setConfiguration("diffpatch")
                .setMainClass("codechicken.diffpatch.DiffPatch")
                .setArgs(Arrays.asList("--patch", Paths.DIR_SRC, Paths.DIR_PATCHES, "--output", Paths.DIR_SRC,
                        "--reject", Paths.DIR_TEMP + "patch_rejects/"))
                .setMessage("[acp.decomp] Step -> Patching source files...");
        patch.exec(logger, true); // condition???

        Step repackageDefaults = new RepackageDefaultsStep()
                .setInputDirectory(project.file(Paths.DIR_SRC))
                .setOutputDirectory(project.file(Paths.DIR_SRC))
                .setMessage("[acp.decomp] Step -> Repackaging default-level source files...");
        repackageDefaults.exec(logger, true); // condition???

        Step copyResources = new ExtractFileStep()
                .setProject(project)
                .setInput(project.file(Paths.EXTRA_JAR))
                .setOutput(project.file(Paths.DIR_RESOURCES))
                .setExclusions(Arrays.asList("com/jcraft/**", "paulscode/**", "META-INF/**"))
                .setMessage("[acp.decomp] Step -> Extracting JAR resources...");
        copyResources.exec(logger, project.file(Paths.DIR_RESOURCES).listFiles() == null);

        Step backupSrc = new CopyFileStep()
                .setProject(project)
                .setInput(project.file(Paths.DIR_SRC))
                .setOutput(project.file(Paths.DIR_VANILLA_SRC))
                .setExclusions("acp/")
                .setMessage("[acp.decomp] Step -> Backing up source files...");
        backupSrc.exec(logger, project.file(Paths.DIR_VANILLA_SRC).listFiles() == null);

        Step backupResources = new CopyFileStep()
                .setProject(project)
                .setInput(project.file(Paths.DIR_RESOURCES))
                .setOutput(project.file(Paths.DIR_VANILLA_RESOURCES))
                .setMessage("[acp.decomp] Step -> Backing up JAR resources...");
        backupResources.exec(logger, project.file(Paths.DIR_VANILLA_RESOURCES).listFiles() == null);

        Step testCompile = new JavaCompileStep()
                .setProject(project)
                .setMessage("[acp.decomp] Step -> Test compiling...");
        testCompile.exec(logger, project.file(Paths.DIR_VANILLA_CLASSES).listFiles() == null);

        Step makeVanillaHashes = new MakeVanillaHashesStep()
                .setProject(project)
                .setMessage("[acp.decomp] Step -> Making vanilla hashes...");
        makeVanillaHashes.exec(logger, !project.file("build/modding/hashes/vanilla.md5").exists());
    }
}
