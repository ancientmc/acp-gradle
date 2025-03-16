package com.ancientmc.acp;

import com.ancientmc.acp.tasks.*;
import com.ancientmc.acp.tasks.step.ResolveToolsStep;
import com.ancientmc.acp.tasks.step.Step;
import com.ancientmc.acp.util.Paths;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AcpPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        String minecraftVersion = project.getProperties().get("minecraft_version").toString();
        AcpExtension extension = project.getExtensions().create("acp", AcpExtension.class, project);

        project.getPluginManager().apply(JavaPlugin.class);

        TaskProvider<Initialize> initialize = project.getTasks().register("initialize", Initialize.class);
        TaskProvider<Decompile> decompile = project.getTasks().register("decompile", Decompile.class);

        /*
        TaskProvider<JavaCompile> testCompile = project.getTasks().register("testCompile", JavaCompile.class);
        TaskProvider<MakeHashes> makeVanillaHashes = project.getTasks().register("makeVanillaHashes", MakeHashes.class);
         */

        TaskProvider<DownloadModLoader> downloadModLoader = project.getTasks().register("downloadModLoader", DownloadModLoader.class);
        TaskProvider<JavaExec> makeDiffPatches = project.getTasks().register("makeDiffPatches", JavaExec.class);
        TaskProvider<MakeHashes> makeModdedHashes = project.getTasks().register("makeModdedHashes", MakeHashes.class);
        TaskProvider<MakeReobfSrg> makeReobfSrg = project.getTasks().register("makeReobfSrg", MakeReobfSrg.class);
        TaskProvider<JavaExec> reobfJar = project.getTasks().register("reobfJar", JavaExec.class);
        TaskProvider<Copy> extractReobfClasses = project.getTasks().register("extractReobfClasses", Copy.class);
        TaskProvider<MakeArchives> makeArchives = project.getTasks().register("makeArchives", MakeArchives.class);

        List<String> configurations = Arrays.asList("jarsplitter", "mcinjector", "autorenamingtool", "fernflower", "diffpatch", "binpatch", "specialsource");
        configurations.forEach(cfg -> project.getConfigurations().create(cfg));

        initialize.configure(task -> {
            task.setGroup("acp");
            task.setDescription("Initializes the ACP workspace by downloading necessary files for decompilation.");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        project.afterEvaluate(proj -> {

            // Initialize workspace.
            initialize.get().exec();

            // Generate the diffpatch directory for modding. Done to avoid I/O whining from Java.
            try {
                if (!proj.file(Paths.DIR_MODDED_PATCHES).exists()) {
                    FileUtils.forceMkdir(proj.file(Paths.DIR_MODDED_PATCHES));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        decompile.configure(task -> {
            task.setGroup("acp");
            task.setDescription("Decompiles Minecraft's source code.");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        /* Commented out until we figure out how to execute them within a Step.
        testCompile.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Compiles the game and stores the class files in the build directory. These class files are used when making the hashes that get compared during the mod archive creation process.");
            task.setSource(project.file(Paths.DIR_SRC));
            task.setClasspath(project.getExtensions().getByType(SourceSetContainer.class).getByName("main").getCompileClasspath());
            task.getDestinationDirectory().set(project.file(Paths.DIR_VANILLA_CLASSES));
            task.getOptions().setCompilerArgs(Arrays.asList("-g:none", "-source", "8", "-target", "8"));
            task.exclude("acp/");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        makeVanillaHashes.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Creates the hashes for the vanilla Minecraft files (or vanilla files plus the mod loader patches applied by the end user).");
            task.getClassesDirectory().set(project.file(Paths.DIR_VANILLA_CLASSES));
            task.getResourcesDirectory().set(project.file(Paths.DIR_VANILLA_RESOURCES));
            task.getOutput().set(project.file("build/modding/hashes/vanilla.md5"));
        });
         */

        downloadModLoader.configure(task -> {
            String loaderType = extension.getLoader().get();
            task.setGroup("modtools");
            task.setDescription("Downloads the mod loader and modding API for the selected Minecraft version.");
            task.getVersion().set(minecraftVersion);
            task.getOutputDir().set(project.file(Paths.DIR_MODPATCHES));
            task.getModLoader().set(loaderType);
        });

        makeDiffPatches.configure(task -> {
            task.setGroup("modtools");
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(project.getConfigurations().findByName("diffpatch")));
            task.args("--diff", Paths.DIR_VANILLA_SRC, Paths.DIR_SRC, "--output", Paths.DIR_MODDED_PATCHES);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
            task.setIgnoreExitValue(true);
        });

        makeReobfSrg.configure(task -> {
            task.setGroup("modtools");
            task.setDescription("");
            task.getInputSrg().set(project.file(Paths.TSRG));
            task.getOutputSrg().set(project.file(Paths.REOBF_SRG));
        });

        reobfJar.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(":jar", makeReobfSrg);
            task.getMainClass().set("net.md_5.specialsource.SpecialSource");
            task.setClasspath(project.files(project.getConfigurations().findByName("specialsource")));
            task.args("--in-jar", Paths.INTERM_JAR, "--out-jar", Paths.REOBF_JAR, "--srg-in", Paths.REOBF_SRG, "--reverse");
            task.getLogging().captureStandardError(LogLevel.LIFECYCLE);
        });

        extractReobfClasses.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(reobfJar);
            task.from(project.zipTree(project.file(Paths.REOBF_JAR))).include("*.class", "net/");
            task.into(Paths.DIR_REOBF_CLASSES);
        });

        makeModdedHashes.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(extractReobfClasses);
            task.getClassesDirectory().set(project.file(Paths.DIR_MODDED_CLASSES));
            task.getResourcesDirectory().set(project.file(Paths.DIR_RESOURCES));
            task.getOutput().set(project.file("build/modding/hashes/modded.md5"));
        });

        makeArchives.configure(task -> {
            String name = extension.getModName().get();
            task.setGroup("modtools");
            task.dependsOn(makeModdedHashes);
            task.getObfuscatedClassDirectory().set(project.file(Paths.DIR_REOBF_CLASSES));
            task.getResourcesDirectory().set(project.file(Paths.DIR_RESOURCES));
            task.getHashDirectory().set(project.file("build/modding/hashes/"));
            task.getSrg().set(project.file(Paths.TSRG));
            task.getArchiveDirectory().set(project.file("build/modding/archives/" + name + "/"));
        });
    }
}