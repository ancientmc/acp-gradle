package com.ancientmc.acp.task;

import com.ancientmc.acp.AcpExtension;
import com.ancientmc.acp.task.step.*;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;

/**
 * Builds the mod's files for distribution: DiffPatches, LZMA binary patches, and ZIPs/TARs containing compiled classes.
 * @author moist-mason
 */
public abstract class BuildMod extends AcpTask {

    @TaskAction
    public void exec() {
        Project project = getProject();
        AcpExtension extension = project.getExtensions().getByType(AcpExtension.class);
        String lzmaPath = Paths.DIR_BUILD_MODDED + "patches/bin/" + extension.getModName().get() + "-" + project.getVersion() + ".lzma";
        setLogger();

        Step moddedCompile = new JavaCompileStep(project, logger, "Compiling the game")
                .setSourceDirectory(project.file(Paths.DIR_SRC))
                .setClasspathCollection(project.getExtensions().getByType(SourceSetContainer.class).named("main").get().getCompileClasspath())
                .setNativesDirectory(project.file(Paths.DIR_NATIVES))
                .setOutputDirectory(project.file(Paths.DIR_MODDED_CLASSES))
                .setCondition(true); // Most of the conditionals in this task are true to ensure the build process occurs with every change a user makes to their mod.
        moddedCompile.exec();

        Step makeDiffPatches = new JavaExecStep(project, logger, "Making DIFF patches")
                .setConfiguration("diffpatch")
                .setMainClass("codechicken.diffpatch.DiffPatch")
                .setArgs(Arrays.asList("--diff", Paths.DIR_VANILLA_SRC, Paths.DIR_SRC, "--output", Paths.DIR_MODDED_PATCHES + "/diff/"))
                .setCondition(true);
        makeDiffPatches.exec();

        Step makeReobfSrg = new MakeReobfSrg(project, logger, "Making SRG for reobfuscation")
                .setInput(project.file(Paths.TSRG))
                .setOutput(project.file(Paths.REOBF_SRG))
                .setCondition(!project.file(Paths.REOBF_SRG).exists());
        makeReobfSrg.exec();

        Step buildJar = new BuildJar(project, logger, "Building JAR")
                .setClassDirectory(project.file(Paths.DIR_MODDED_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_RESOURCES))
                .setOutput(project.file(Paths.INTERM_JAR))
                .setCondition(true);
        buildJar.exec();

        Step reobfJar = new JavaExecStep(project, logger, "Reobfuscating JAR")
                .setConfiguration("specialsource")
                .setMainClass("net.md_5.specialsource.SpecialSource")
                .setArgs(Arrays.asList("--in-jar", Paths.INTERM_JAR, "--out-jar", Paths.REOBF_JAR, "--srg-in", Paths.REOBF_SRG))
                .setCondition(true);
        reobfJar.exec();

        Step makeBinPatches = new JavaExecStep(project, logger, "Making binary patches")
                .setConfiguration("binpatch")
                .setMainClass("net.neoforged.binarypatcher.ConsoleTool")
                .setArgs(Arrays.asList("--clean", Paths.VANILLA_JAR, "--dirty", Paths.INTERM_JAR, "--output", lzmaPath, "--srg", Paths.REOBF_SRG))
                .setCondition(true);
        makeBinPatches.exec();

        Step extractReobfClasses = new ExtractFile(project, logger, "Extracting reobfuscated classes")
                .setInput(project.file(Paths.REOBF_JAR))
                .setOutput(project.file(Paths.DIR_REOBF_CLASSES))
                .setInclusions(Arrays.asList("*.class", "**/*.class"))
                .setCondition(true);
        extractReobfClasses.exec();

        Step makeModdedHashes = new MakeHashes(project, logger, "Generating modded hashes")
                .setClassDirectory(project.file(Paths.DIR_MODDED_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_RESOURCES))
                .setOutput(project.file(Paths.MODDED_HASH_FILE))
                .setCondition(true);
        makeModdedHashes.exec();

        Step makeArchives = new MakeArchives(project, logger, "Compressing ZIP and TAR archives")
                .setSrg(project.file(Paths.TSRG))
                .setObfDirectory(project.file(Paths.DIR_REOBF_CLASSES))
                .setResourceDirectory(project.file(Paths.DIR_RESOURCES))
                .setHashDirectory(project.file(Paths.DIR_BUILD_MODDED + "hashes/"))
                .setArchiveDirectory(project.file(Paths.DIR_BUILD_MODDED + "archives/" + extension.getModName().get() + "/"))
                .setCondition(true);
        makeArchives.exec();

        logger.write();
    }
}
