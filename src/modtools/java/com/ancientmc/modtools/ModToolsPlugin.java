package com.ancientmc.modtools;

import com.ancientmc.acp.tasks.GenerateHashes;
import com.ancientmc.acp.utils.Paths;
import com.ancientmc.modtools.tasks.DownloadModLoader;
import com.ancientmc.modtools.tasks.MakeZip;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;

public class ModToolsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        String minecraftVersion = project.getExtensions().getExtraProperties().get("MC_VERSION").toString();
        ModToolsExtension extension = project.getExtensions().create("modtools", ModToolsExtension.class, project);

        project.getPluginManager().apply(JavaPlugin.class);

        Configuration binpatcher = project.getConfigurations().getByName("binpatcher");
        Configuration diffpatch = project.getConfigurations().getByName("diffpatch");

        TaskProvider<DownloadModLoader> downloadModLoader = project.getTasks().register("downloadModLoader", DownloadModLoader.class);
        TaskProvider<JavaExec> genDiffPatches = project.getTasks().register("genDiffPatches", JavaExec.class);
        TaskProvider<JavaExec> genBinPatches = project.getTasks().register("genBinPatches", JavaExec.class);
        TaskProvider<GenerateHashes> generateModdedHashes = project.getTasks().register("generateModdedHashes", GenerateHashes.class);
        TaskProvider<Copy> extractReobfClasses = project.getTasks().register("extractReobfClasses", Copy.class);
        TaskProvider<MakeZip> makeZip = project.getTasks().register("makeZip", MakeZip.class);

        project.afterEvaluate(proj -> {
            String diffPatches = extension.getDiffPatchesDir().get();
            String binPatches = extension.getBinPatchesDir().get();
            try {
                if (!proj.file(diffPatches).exists()) {
                    FileUtils.forceMkdir(proj.file(diffPatches));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                if (!proj.file(binPatches).exists()) {
                    FileUtils.forceMkdir(proj.file(binPatches));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        downloadModLoader.configure(task -> {
            String loaderType = extension.getType().get();

            task.setGroup("modtools");
            task.getVersion().set(minecraftVersion);
            task.getOutputDir().set(project.file(Paths.DIR_MODLOADER_PATCHES));
            task.getModLoader().set(loaderType);
        });

        genDiffPatches.configure(task -> {
            String diffPatches = extension.getDiffPatchesDir().get();
            task.setGroup("modtools");
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(diffpatch));
            task.args("--diff", Paths.DIR_ORIGINAL_SRC, Paths.DIR_SRC, "--output", diffPatches);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
            task.setIgnoreExitValue(true);
        });

        genBinPatches.configure(task -> {
            String binPatches = extension.getDiffPatchesDir().get();
            task.setGroup("modtools");
            task.getMainClass().set("net.minecraftforge.binarypatcher.ConsoleTool");
            task.setClasspath(project.files(binpatcher));
            task.args("--clean", Paths.BASE_JAR, "--dirty", Paths.REOBF_JAR, "--output", binPatches);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        generateModdedHashes.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(":compileJava");
            task.getClassesDirectory().set(project.file(project.getBuildDir().getPath() + "\\classes\\java\\main\\"));
            task.getOutput().set(new File("build\\modding\\hashes\\modded.md5"));
        });

        extractReobfClasses.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(":reobfJar");
            task.from(project.zipTree(project.file(Paths.REOBF_JAR))).include("*.class", "net\\");
            task.into(Paths.DIR_REOBF_CLASSES);
        });

        makeZip.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(generateModdedHashes, extractReobfClasses);
            task.getClassDirectory().set(project.file(project.getBuildDir().getPath() + "\\classes\\java\\main\\"));
            task.getObfuscatedClassDirectory().set(project.file(Paths.DIR_REOBF_CLASSES));
            task.getOriginalHash().set(project.file("build\\modding\\hashes\\vanilla.md5"));
            task.getModdedHash().set(project.file("build\\modding\\hashes\\modded.md5"));
            task.getSrg().set(project.file(Paths.SRG));
            task.getZip().set(project.file("build\\modding\\zip\\mod.zip"));
        });
    }
}
