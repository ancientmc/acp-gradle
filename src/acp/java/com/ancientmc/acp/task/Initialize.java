package com.ancientmc.acp.task;

import com.ancientmc.acp.AcpExtension;
import com.ancientmc.acp.task.step.*;
import com.ancientmc.acp.task.step.function.ResolveLibraries;
import com.ancientmc.acp.task.step.function.ResolveTools;
import com.ancientmc.acp.task.step.io.*;
import com.ancientmc.acp.util.Json;
import com.ancientmc.acp.util.Paths;
import com.ancientmc.acp.util.Util;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

public abstract class Initialize extends DefaultTask {
    private static final String PHASE = "init";

    @TaskAction
    public void exec() {
        Project project = getProject();
        AcpExtension extension = project.getExtensions().getByType(AcpExtension.class);
        String version = Util.getMinecraftVersion(project);
        Logger logger = project.getLogger();

        try {
            Step startupMessage = new Step()
                    .setMessage(getStartupMessage(project, version));
            startupMessage.exec(logger, !(project.file(Paths.DIR_CFG).exists() && project.file(Paths.DIR_RUN).exists()));

            Step downloadAcpData = new DownloadFile()
                    .setInput(Util.toMavenUrl(Util.getAncientMCMaven(), extension.getData().get(), "zip"))
                    .setOutput(project.file(Paths.ACP_DATA))
                    .setMessage(PHASE, "Downloading ACP data");
            downloadAcpData.exec(logger, !downloadAcpData.getOutput().exists());

            Step extractAcpData = new ExtractFile()
                    .setInput(downloadAcpData.getOutput())
                    .setOutput(project.file(Paths.DIR_CFG))
                    .setProject(project)
                    .setMessage(PHASE, "Extracting ACP data");
            extractAcpData.exec(logger, !project.file(Paths.TSRG).exists());

            Step downloadVersionManifest = new DownloadFile()
                    .setInput(Util.getUrl("https://raw.githubusercontent.com/ancientmc/AcpGen/refs/heads/classic/data/versions/version_manifest.json"))
                    .setOutput(project.file(Paths.VERSION_MANIFEST))
                    .setMessage(PHASE, "Downloading version manifest");
            downloadVersionManifest.exec(logger, !downloadVersionManifest.getOutput().exists());

            Step downloadJson = new DownloadFile()
                    .setInput(Json.getJsonUrl(downloadVersionManifest.getOutput(), version))
                    .setOutput(project.file(Paths.JSON))
                    .setMessage(PHASE, "Downloading version JSON");
            downloadJson.exec(logger, !downloadJson.getOutput().exists());

            Step resolveLibraries = new ResolveLibraries()
                    .setLibraries(Json.getLibraries(Arrays.asList(downloadJson.getOutput(), project.file(Paths.DIR_CFG + "jardep.json"))))
                    .setProject(project);
            resolveLibraries.exec();

            Step resolveTools = new ResolveTools()
                    .setProject(project)
                    .setProperties(project.file("gradle.properties"));
            resolveTools.exec();

            Step extractNatives = new ExtractNatives()
                    .setUrls(Json.getNativeUrls(downloadJson.getOutput()))
                    .setProject(project)
                    .setOutput(project.file(Paths.DIR_NATIVES))
                    .setMessage(PHASE, "Extracting natives");
            extractNatives.exec(logger, !extractNatives.getOutput().exists());

            Step downloadAssets = new DownloadAssets()
                    .setIndex(Json.getAssetIndexUrl(project.file(Paths.JSON)))
                    .setOutput(project.file(Paths.DIR_ASSETS))
                    .setMessage(PHASE, "Downloading assets");
            downloadAssets.exec(logger, Util.directoryCondition(project.file(Paths.DIR_ASSETS)));

            Step downloadClient = new DownloadJar()
                    .setInput(Json.getJarUrl(downloadJson.getOutput(), "client"))
                    .setOutput(project.file(Paths.DIR_TEMP))
                    .setMessage(PHASE, "Downloading client JAR");
            downloadClient.exec(logger, !project.file(Paths.BASE_JAR).exists()); // Fails if downloadClient.getOutput() is used here. Probably bc that isn't used in another step.

            Step copyStart = new CopyFile()
                    .setProject(project)
                    .setInput(project.file(Paths.DIR_START))
                    .setOutput(project.file(Paths.DIR_SRC + "acp/client/"))
                    .setMessage(PHASE, "Copying start files");
            copyStart.exec(logger, !project.file(Paths.DIR_SRC + "acp/client/Start.java").exists());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the startup message used upon booting the ACP initializer for the first time.
     * @param project The gradle project.
     * @param minecraftVersion The Minecraft version.
     * @return The message.
     * @throws IOException exception.
     */
    private static String getStartupMessage(Project project, String minecraftVersion) throws IOException {
        List<String> lines = Arrays.asList("Ancient Coder Pack",
                "Copyright (c) AncientMC",
                "ACP Version: " + project.getProperties().get("acp_version").toString(),
                "ACP-Gradle Version: " + getPluginVersion(project),
                "Minecraft Version: " + minecraftVersion,
                "[acp.init] Initializing ACP");

        return String.join("\n", lines) + "\n";
    }

    /**
     * Returns the ACP Gradle version by parsing its JAR manifest.
     * @param project The gradle project.
     * @return The ACP Gradle version.
     */
    private static String getPluginVersion(Project project) throws IOException {
        Plugin<?> plugin = project.getPlugins().stream().filter(p -> p.getClass().getName().contains("acp")).findAny().orElse(null);

        if (plugin != null) {
            URLClassLoader loader = (URLClassLoader) plugin.getClass().getClassLoader();
            Manifest manifest = new Manifest(loader.findResource("META-INF/MANIFEST.MF").openStream());
            return manifest.getMainAttributes().getValue("Implementation-Version");
        }

        return null;
    }
}
