package com.ancientmc.acp.init;

import com.ancientmc.acp.ACPExtension;
import com.ancientmc.acp.init.step.*;
import com.ancientmc.acp.utils.Json;
import com.ancientmc.acp.utils.Paths;
import com.ancientmc.acp.utils.Utils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class ACPInitialization {

    public static void init(Project project, ACPExtension extension, String version) throws IOException {
        String maven = Utils.getAncientMCMaven();
        String data = extension.getData().get();
        Logger logger = project.getLogger();

        DownloadFileStep downloadACPData = new DownloadFileStep()
                .setInput(new URL(Utils.toMavenUrl(maven, data, "zip")))
                .setOutput(project.file(Paths.DIR_CFG + "data.zip"));
        downloadACPData.printMessage(logger, "Downloading ACP data", !downloadACPData.getOutput().exists());
        downloadACPData.exec();

        ExtractFileStep extractACPData = new ExtractFileStep()
                .setInput(downloadACPData.getOutput())
                .setOutput(project.file(Paths.DIR_CFG))
                .setProject(project);
        extractACPData.printMessage(logger, "Extracting ACP data", !downloadACPData.getOutput().exists());
        extractACPData.exec();

        DownloadFileStep downloadVersionManifest = new DownloadFileStep()
                .setInput(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                .setOutput(project.file(Paths.DIR_TEMP + "version_manifest.json"));
        downloadVersionManifest.printMessage(logger, "Downloading version manifest", !downloadVersionManifest.getOutput().exists());
        downloadVersionManifest.exec();

        DownloadFileStep downloadJson = new DownloadFileStep()
                .setInput(Json.getJsonUrl(downloadVersionManifest.getOutput(), version))
                .setOutput(project.file(Paths.DIR_TEMP + version + ".json"));
        downloadJson.printMessage(logger, "Downloading version JSON", !downloadJson.getOutput().exists());
        downloadJson.exec();

        DownloadLibrariesStep downloadLibraries = new DownloadLibrariesStep()
                .setLibraries(Json.getLibraries(Arrays.asList(downloadJson.getOutput(), project.file(Paths.DIR_CFG + "jardep.json"))))
                .setProject(project);
        downloadLibraries.exec();

        ExtractNativesStep extractNatives = new ExtractNativesStep()
                .setUrls(Json.getNativeUrls(downloadJson.getOutput()))
                .setProject(project)
                .setOutput(project.file(Paths.DIR_NATIVES));
        extractNatives.printMessage(logger, "Extracting natives", !extractNatives.getOutput().exists());
        extractNatives.exec();

        DownloadAssetsStep downloadAssets = new DownloadAssetsStep()
                .setJson(downloadJson.getOutput())
                .setOutput(project.file(Paths.DIR_RUN));
        downloadAssets.printMessage(logger, "Downloading assets", !project.file(Paths.DIR_RUN + "resources\\").exists());
        downloadAssets.exec();

        DownloadJarStep downloadJar = new DownloadJarStep()
                .setInput(Json.getJarUrl(downloadJson.getOutput(), "client"))
                .setOutput(project.file(Paths.DIR_TEMP))
                .setVersion(version);
        downloadJar.printMessage(logger, "Downloading client JAR", !(project.file(Paths.BASE_JAR).exists()));
        downloadJar.exec();
    }
}
