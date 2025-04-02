package com.ancientmc.acp.task;

import com.ancientmc.acp.AcpExtension;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.task.step.function.ResolveLibraries;
import com.ancientmc.acp.task.step.function.ResolveTools;
import com.ancientmc.acp.task.step.function.StartupMessage;
import com.ancientmc.acp.task.step.io.*;
import com.ancientmc.acp.util.Json;
import com.ancientmc.acp.util.Paths;
import com.ancientmc.acp.util.Util;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.Arrays;

public abstract class Initialize extends DefaultTask {
    private static final String PHASE = "init";

    @TaskAction
    public void exec() {
        Project project = getProject();
        AcpExtension extension = project.getExtensions().getByType(AcpExtension.class);
        String version = Util.getMinecraftVersion(project);

        try {
            Step startupMessage = new StartupMessage(project)
                    .setMinecraftVersion(version)
                    .setCondition(!project.file(Paths.DIR_CFG).exists() && !project.file(Paths.DIR_RUN).exists());
            startupMessage.exec();

            Step downloadAcpData = new DownloadFile(project, PHASE, "Downloading ACP data")
                    .setInput(Util.toMavenUrl(Util.getAncientMcMaven(), extension.getData().get(), "zip"))
                    .setOutput(project.file(Paths.ACP_DATA))
                    .setCondition(!project.file(Paths.ACP_DATA).exists());
            downloadAcpData.exec();

            Step extractAcpData = new ExtractFile(project, PHASE, "Extracting ACP data")
                    .setInput(project.file(Paths.ACP_DATA))
                    .setOutput(project.file(Paths.DIR_CFG))
                    .setCondition(!project.file(Paths.TSRG).exists());
            extractAcpData.exec();

            Step downloadVersionManifest = new DownloadFile(project, PHASE, "Downloading version manifest")
                    .setInput(Util.getUrl("https://raw.githubusercontent.com/ancientmc/AcpGen/refs/heads/classic/data/versions/version_manifest.json"))
                    .setOutput(project.file(Paths.VERSION_MANIFEST))
                    .setCondition(!project.file(Paths.VERSION_MANIFEST).exists());
            downloadVersionManifest.exec();

            Step downloadJson = new DownloadFile(project, PHASE, "Downloading version JSON")
                    .setInput(Json.getJsonUrl(project.file(Paths.VERSION_MANIFEST), version))
                    .setOutput(project.file(Paths.JSON))
                    .setCondition(!project.file(Paths.JSON).exists());
            downloadJson.exec();

            Step resolveLibraries = new ResolveLibraries(project, PHASE, "Resolving Minecraft libraries")
                    .setLibraries(Json.getLibraries(Arrays.asList(project.file(Paths.JSON), project.file(Paths.DIR_CFG + "jardep.json"))))
                    .setCondition(true);
            resolveLibraries.exec();

            Step resolveTools = new ResolveTools(project, PHASE, "Resolving ACP tools")
                    .setProperties(project.file("gradle.properties"))
                    .setCondition(true);
            resolveTools.exec();

            Step extractNatives = new ExtractNatives(project, PHASE, "Extracting natives")
                    .setOutput(project.file(Paths.DIR_NATIVES))
                    .setUrls(Json.getNativeUrls(project.file(Paths.JSON)))
                    .setCondition(!project.file(Paths.DIR_NATIVES).exists());
            extractNatives.exec();

            Step downloadAssets = new DownloadAssets(project, PHASE, "Downloading assets")
                    .setIndex(Json.getAssetIndexUrl(project.file(Paths.JSON)))
                    .setOutput(project.file(Paths.DIR_ASSETS))
                    .setCondition(Util.directoryCondition(project.file(Paths.DIR_ASSETS)));
            downloadAssets.exec();

            Step downloadClient = new DownloadJar(project, PHASE, "Downloading client JAR")
                    .setInput(Json.getJarUrl(project.file(Paths.JSON), "client"))
                    .setOutput(project.file(Paths.DIR_TEMP))
                    .setCondition(!project.file(Paths.CLIENT_JAR).exists());
            downloadClient.exec();

            Step copyStart = new CopyFile(project, PHASE, "Copying start files")
                    .setInput(project.file(Paths.DIR_START))
                    .setOutput(project.file(Paths.DIR_SRC + "acp/client/"))
                    .setCondition(!project.file(Paths.DIR_SRC + "acp/client/Start.java").exists());
            copyStart.exec();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
