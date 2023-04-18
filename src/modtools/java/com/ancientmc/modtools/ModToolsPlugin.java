package com.ancientmc.modtools;

import com.ancientmc.acp.utils.Paths;
import com.ancientmc.modtools.tasks.DownloadModLoader;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;

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
            task.args("--diff", Paths.DIR_BACKUP_SRC, Paths.DIR_SRC, "--output", diffPatches);
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
    }
}
