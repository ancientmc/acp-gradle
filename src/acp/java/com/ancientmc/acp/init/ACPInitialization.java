package com.ancientmc.acp.init;

import com.ancientmc.acp.ACPExtension;
import com.ancientmc.acp.init.step.*;
import com.ancientmc.acp.utils.Json;
import com.ancientmc.acp.utils.Paths;
import com.ancientmc.acp.utils.Utils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Initialization class for events that occur upon launching ACP for the first time, or upon a gradle refresh if needed.
 */
public class ACPInitialization {

    /**
     * Initialization method.
     * @param project The gradle project.
     * @param extension The ACP plugin extension. Contains the maven path for the ACP data, which is converted into a URL.
     * @param version The minecraft version, specified in the ACP end-user workspace.
     * @throws IOException
     */
    public static void init(Project project, ACPExtension extension, String version) throws IOException {
        String maven = Utils.getAncientMCMaven();
        String data = extension.getData().get();
        Logger logger = project.getLogger();

        DownloadFileStep downloadACPData = (DownloadFileStep) new DownloadFileStep()
                .setInput(new URL(Utils.toMavenUrl(maven, data, "zip")))
                .setOutput(project.file(Paths.DIR_CFG + "data.zip"))
                .setMessage("Downloading ACP data");
        downloadACPData.exec(logger, !downloadACPData.getOutput().exists());

        ExtractFileStep extractACPData = (ExtractFileStep) new ExtractFileStep()
                .setInput(downloadACPData.getOutput())
                .setOutput(project.file(Paths.DIR_CFG))
                .setProject(project)
                .setMessage("Extracting ACP data");
        extractACPData.exec(logger, !project.file(Paths.SRG).exists());

        DownloadFileStep downloadVersionManifest = (DownloadFileStep) new DownloadFileStep()
                .setInput(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                .setOutput(project.file(Paths.DIR_TEMP + "version_manifest.json"))
                .setMessage("Downloading version manifest");
        downloadVersionManifest.exec(logger, !downloadVersionManifest.getOutput().exists());

        DownloadFileStep downloadJson = (DownloadFileStep) new DownloadFileStep()
                .setInput(Json.getJsonUrl(downloadVersionManifest.getOutput(), version))
                .setOutput(project.file(Paths.DIR_TEMP + version + ".json"))
                .setMessage("Downloading version JSON");
        downloadJson.exec(logger, !downloadJson.getOutput().exists());

        DownloadLibrariesStep downloadLibraries = (DownloadLibrariesStep) new DownloadLibrariesStep()
                .setLibraries(Json.getLibraries(Arrays.asList(downloadJson.getOutput(), project.file(Paths.DIR_CFG + "jardep.json"))))
                .setProject(project);
        downloadLibraries.exec();

        Step downloadToolsStep = (DownloadToolsStep) new DownloadToolsStep()
                .setProject(project)
                .setProperties(project.file("gradle.properties"));
        downloadToolsStep.exec();

        ExtractNativesStep extractNatives = (ExtractNativesStep) new ExtractNativesStep()
                .setUrls(Json.getNativeUrls(downloadJson.getOutput()))
                .setProject(project)
                .setOutput(project.file(Paths.DIR_NATIVES))
                .setMessage("Extracting natives");
        extractNatives.exec(logger, !extractNatives.getOutput().exists());

        DownloadAssetsStep downloadAssets = (DownloadAssetsStep) new DownloadAssetsStep()
                .setIndex(Json.getAssetIndexUrl(downloadJson.getOutput()))
                .setOutput(project.file(Paths.DIR_RUN))
                .setMessage("Downloading assets");
        downloadAssets.exec(logger, !project.file(Paths.DIR_RUN + "resources\\").exists());

        DownloadJarStep downloadJar = (DownloadJarStep) new DownloadJarStep()
                .setInput(Json.getJarUrl(downloadJson.getOutput(), "client"))
                .setOutput(project.file(Paths.DIR_TEMP))
                .setVersion(version)
                .setMessage("Downloading client JAR");
        downloadJar.exec(logger, !project.file(Paths.BASE_JAR).exists());
    }
}
