package com.ancientmc.acp;

import com.ancientmc.acp.task.BuildMod;
import com.ancientmc.acp.task.Decompile;
import com.ancientmc.acp.task.DownloadModLoader;
import com.ancientmc.acp.task.Initialize;
import com.ancientmc.acp.util.Paths;
import com.ancientmc.acp.util.Util;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AcpPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        String minecraftVersion = Util.getMinecraftVersion(project);
        AcpExtension extension = project.getExtensions().create("acp", AcpExtension.class, project);

        project.getPluginManager().apply(JavaPlugin.class);

        TaskProvider<Initialize> initialize = project.getTasks().register("initialize", Initialize.class);
        TaskProvider<Decompile> decompile = project.getTasks().register("decompile", Decompile.class);
        TaskProvider<BuildMod> buildMod = project.getTasks().register("buildMod", BuildMod.class);
        TaskProvider<DownloadModLoader> downloadModLoader = project.getTasks().register("downloadModLoader", DownloadModLoader.class);
        TaskProvider<JavaExec> runClient = project.getTasks().register("runClient", JavaExec.class);

        List<String> configurations = Arrays.asList("jarsplitter", "mcinjector", "autorenamingtool", "fernflower", "diffpatch", "binpatch", "specialsource");
        configurations.forEach(cfg -> project.getConfigurations().create(cfg));

        // Set Minecraft to compile against Java 8.
        project.getTasks().named("compileJava", JavaCompile.class).configure(action -> {
            action.getOptions().setCompilerArgs(Arrays.asList("-g:none", "-source", "8", "-target", "8"));
        });

        initialize.configure(task -> {
            task.setGroup("acp");
            task.setDescription("Initializes the ACP workspace by downloading necessary files for decompilation.");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        decompile.configure(task -> {
            task.setGroup("acp");
            task.setDescription("Decompiles Minecraft's source code.");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        buildMod.configure(task -> {
            task.setGroup("acp");
            task.setDescription("Builds archives, LZMA files, and Patch files for modders to share.");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        downloadModLoader.configure(task -> {
            task.setGroup("acp");
            task.setDescription("Downloads the mod loader and modding API for the selected Minecraft version.");
            task.getVersion().set(minecraftVersion);
            task.getOutputDir().set(project.file(Paths.DIR_MODPATCHES));
            task.getModLoader().set(extension.getLoader().get());
        });

        runClient.configure(task -> {
            task.setGroup("acp");
            task.setDescription("Runs Minecraft.");
            task.setClasspath(project.getExtensions().getByType(SourceSetContainer.class).getByName("main").getRuntimeClasspath());
            task.getMainClass().set("acp.client.Start");
            task.setSystemProperties(Collections.singletonMap("java.library.path", Paths.DIR_NATIVES));
        });

        project.afterEvaluate(proj -> {
            initialize.get().exec(); // Initialize workspace.
            makeDirs(proj); // Make required directories not made elsewhere.
        });
    }

    public static void makeDirs(Project project) {
        List<File> dirs = Arrays.asList(project.file(Paths.DIR_MODDED_PATCHES), project.file(Paths.DIR_REOBF_CLASSES));

        dirs.forEach(dir -> {
            try {
                if (!dir.exists()) {
                    Files.createDirectories(dir.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}