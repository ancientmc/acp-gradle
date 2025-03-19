package com.ancientmc.acp.task;

import com.ancientmc.acp.AcpExtension;
import com.ancientmc.acp.task.step.function.*;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.task.step.io.ExtractFile;
import com.ancientmc.acp.util.Paths;
import com.ancientmc.acp.util.Util;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;
import java.util.List;

public class BuildMod extends DefaultTask {
    public static final String PHASE = "mod";

    @TaskAction
    public void exec() {
        Project project = getProject();
        Logger logger = project.getLogger();
        AcpExtension extension = (AcpExtension) project.getExtensions().getByName("acp");
        String lzmaPath = "build/modding/patches/bin/" + extension.getModName().get() + "-" + project.getVersion() + ".lzma";

        Step moddedCompile = new JavaCompileStep()
                .setSourceDirectory(project.file(Paths.DIR_SRC))
                .setClasspathCollection(project.getExtensions().getByType(SourceSetContainer.class).named("main").get().getCompileClasspath())
                .setNativesDirectory(project.file(Paths.DIR_NATIVES))
                .setOutputDirectory(project.file(Paths.DIR_MODDED_CLASSES))
                .setMessage(PHASE, "Compiling the game...");
        moddedCompile.exec(logger, Util.directoryCondition(project.file(Paths.DIR_MODDED_CLASSES)));

        Step makeDiffPatches = new JavaExecStep()
                .setProject(project)
                .setConfiguration("diffpatch")
                .setMainClass("codechicken.diffpatch.DiffPatch")
                .setArgs(Arrays.asList("--diff", Paths.DIR_VANILLA_SRC, Paths.DIR_SRC, "--output", Paths.DIR_MODDED_PATCHES + "/diff/"))
                .setMessage(PHASE, "Making DIFF patches...");
        makeDiffPatches.exec(logger, Util.directoryCondition(project.file(Paths.DIR_MODDED_PATCHES + "/diff/")));

        Step makeReobfSrg = new MakeReobfSrg()
                .setProject(project)
                .setInput(project.file(Paths.TSRG))
                .setOutput(project.file(Paths.REOBF_SRG))
                .setMessage(PHASE, "Making SRG for reobfuscation...");
        makeReobfSrg.exec(logger, !project.file(Paths.REOBF_SRG).exists());

        Step buildJar = new BuildJar()
                .setProject(project)
                .setClassDirectory(project.file(Paths.DIR_MODDED_CLASSES))
                .setResources(project.file(Paths.DIR_RESOURCES))
                .setOutput(project.file(Paths.INTERM_JAR))
                .setMessage(PHASE, "Building JAR...");
        buildJar.exec(logger, !project.file(Paths.INTERM_JAR).exists());

        Step reobfJar = new JavaExecStep()
                .setProject(project)
                .setConfiguration("specialsource")
                .setMainClass("net.md_5.specialsource.SpecialSource")
                .setArgs(Arrays.asList("--in-jar", Paths.INTERM_JAR, "--out-jar", Paths.REOBF_JAR, "--srg-in", Paths.REOBF_SRG, "--reverse"))
                .setMessage(PHASE, "Reobfuscating JAR...");
        reobfJar.exec(logger, !project.file(Paths.REOBF_JAR).exists());

        Step makeBinPatches = new JavaExecStep()
                .setProject(project)
                .setConfiguration("binpatch")
                .setMainClass("net.neoforged.binarypatcher.ConsoleTool")
                .setArgs(Arrays.asList("--clean", Paths.MAPPED_JAR, "--dirty", Paths.REOBF_JAR, "--output", lzmaPath))
                .setMessage(PHASE, "Making binary patches...");
        makeBinPatches.exec(logger, !project.file(lzmaPath).exists());

        Step extractReobfClasses = new ExtractFile()
                .setProject(project)
                .setInput(project.file(Paths.REOBF_JAR))
                .setOutput(project.file(Paths.DIR_REOBF_CLASSES))
                .setInclusions(List.of("*.class"))
                .setMessage(PHASE, "Extracting reobfuscated classes...");
        extractReobfClasses.exec(logger, project.file(Paths.DIR_REOBF_CLASSES).listFiles() == null);

        Step makeModdedHashes = new MakeHashes()
                .setProject(project)
                .setClassDirectory(project.file(Paths.DIR_MODDED_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_RESOURCES))
                .setOutput(project.file("build/modding/hashes/modded.md5"))
                .setMessage(PHASE, "Generating modded hashes...");
        makeModdedHashes.exec(logger, !project.file("build/modding/hashes/modded.md5").exists());

        Step makeArchives = new MakeArchives()
                .setProject(project)
                .setSrg(project.file(Paths.TSRG))
                .setObfDirectory(project.file(Paths.DIR_MODDED_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_RESOURCES))
                .setHashDirectory(project.file("build/modding/hashes"))
                .setArchiveDirectory(project.file("build/modding/archives"))
                .setMessage(PHASE, "Compressing ZIP and TAR archives...");
        makeArchives.exec(logger, Util.directoryCondition(project.file("build/modding/archives/")));
    }
}
