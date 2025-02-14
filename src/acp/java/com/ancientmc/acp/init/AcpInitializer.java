package com.ancientmc.acp.init;

import com.ancientmc.acp.AcpExtension;
import com.ancientmc.acp.init.step.*;
import com.ancientmc.acp.util.Json;
import com.ancientmc.acp.util.Paths;
import com.ancientmc.acp.util.Util;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

/**
 * Initialization class for events that occur upon launching ACP for the first time, or upon a gradle refresh if needed.
 */
public class AcpInitializer {

    /**
     * Initialization method.
     * @param project The gradle project.
     * @param extension The ACP plugin extension. Contains the maven path for the ACP data, which is converted into a URL.
     * @param version The Minecraft version, specified in the ACP end-user workspace.
     * @throws IOException exception.
     */
    public static void init(Project project, AcpExtension extension, String version) throws IOException {
        String maven = Util.getAncientMCMaven();
        String data = extension.getData().get();
        Logger logger = project.getLogger();

        Step startupMessage = new Step()
                .setMessage(getStartupMessage(project, version));
        startupMessage.exec(logger, !(project.file(Paths.DIR_CFG).exists() && project.file(Paths.DIR_RUN).exists()));

        Step downloadAcpData = new DownloadFileStep()
                .setInput(Util.toMavenUrl(maven, data, "zip"))
                .setOutput(project.file(Paths.ACP_DATA))
                .setMessage("Downloading ACP data");
        downloadAcpData.exec(logger, !downloadAcpData.getOutput().exists());

        Step extractAcpData = new ExtractFileStep()
                .setInput(downloadAcpData.getOutput())
                .setOutput(project.file(Paths.DIR_CFG))
                .setProject(project)
                .setMessage("Extracting ACP data");
        extractAcpData.exec(logger, !project.file(Paths.SRG).exists());

        Step downloadVersionManifest = new DownloadFileStep()
                .setInput(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                .setOutput(project.file(Paths.VERSION_MANIFEST))
                .setMessage("Downloading version manifest");
        downloadVersionManifest.exec(logger, !downloadVersionManifest.getOutput().exists());

        Step downloadJson = new DownloadFileStep()
                .setInput(Json.getJsonUrl(downloadVersionManifest.getOutput(), version))
                .setOutput(project.file(Paths.JSON))
                .setMessage("Downloading version JSON");
        downloadJson.exec(logger, !downloadJson.getOutput().exists());

        Step resolveLibraries = new ResolveLibrariesStep()
                .setLibraries(Json.getLibraries(Arrays.asList(downloadJson.getOutput(), project.file(Paths.DIR_CFG + "jardep.json"))))
                .setProject(project);
        resolveLibraries.exec();

        Step resolveTools = new ResolveToolsStep()
                .setProject(project)
                .setProperties(project.file("gradle.properties"));
        resolveTools.exec();

        Step extractNatives = new ExtractNativesStep()
                .setUrls(Json.getNativeUrls(downloadJson.getOutput()))
                .setProject(project)
                .setOutput(project.file(Paths.DIR_NATIVES))
                .setMessage("Extracting natives");
        extractNatives.exec(logger, !extractNatives.getOutput().exists());

        Step downloadAssets = new DownloadAssetsStep()
                .setIndex(Json.getAssetIndexUrl(project.file(Paths.JSON)))
                .setOutput(project.file(Paths.DIR_RUN))
                .setMessage("Downloading assets");
        downloadAssets.exec(logger, !project.file(Paths.DIR_RUN + "resources/").exists());

        Step downloadClient = new DownloadJarStep()
                .setInput(Json.getJarUrl(downloadJson.getOutput(), "client"))
                .setOutput(project.file(Paths.DIR_TEMP))
                .setVersion(version)
                .setMessage("Downloading client JAR");
        downloadClient.exec(logger, !project.file(Paths.BASE_JAR).exists()); // Fails if downloadClient.getOutput() is used here. Probably bc that isn't used in another step.
    }

    /**
     * Gets the startup message used upon booting the ACP initializer for the first time.
     * @param project The gradle project.
     * @param minecraftVersion The version of Minecraft being decompiled.
     * @return The message.
     * @throws IOException exception.
     */
    private static String getStartupMessage(Project project, String minecraftVersion) throws IOException {
        List<String> lines = Arrays.asList("Ancient Coder Pack",
                "Copyright (c) AncientMC",
                "ACP Version: " + project.getExtensions().getExtraProperties().get("ACP_VERSION"),
                "ACP-Gradle Version: " + getPluginVersion(project),
                "Minecraft Version: " + minecraftVersion);

        return String.join("\n", lines) + "\n";
    }

    /**
     * Returns the ACP Gradle version by parsing its JAR manifest.
     * @param project The gradle project.
     * @return The ACP Gradle version.
     */
    private static String getPluginVersion(Project project) throws IOException {
        // Unfortunately this may have to be unparamititized (intentional misspelling).
        Plugin plugin = project.getPlugins().stream().filter(p -> p.getClass().getName().contains("acp")).findAny().get();
        URLClassLoader loader = (URLClassLoader) plugin.getClass().getClassLoader();
        Manifest manifest = new Manifest(loader.findResource("META-INF/MANIFEST.MF").openStream());

        return manifest.getMainAttributes().getValue("Implementation-Version");
    }
}
