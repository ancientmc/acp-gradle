package com.ancientmc.acp.task;

import com.ancientmc.acp.AcpExtension;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.task.step.common.CopyFile;
import com.ancientmc.acp.task.step.common.DownloadFile;
import com.ancientmc.acp.task.step.common.ExtractFile;
import com.ancientmc.acp.task.step.init.*;
import com.ancientmc.acp.util.*;
import org.gradle.api.Project;

import java.io.IOException;
import java.util.Arrays;

/**
 * Initializes the workspace.
 * @author moist-mason
 */
public abstract class Initialize extends AcpTask {

    @Override
    public void action(final Project project) {
        try {
            AcpExtension extension = project.getExtensions().getByType(AcpExtension.class);
            String version = Util.getMinecraftVersion(project);

            Step startupMessage = new StartupMessage(project, version, logger)
                    .setCondition(!project.file(Paths.DIR_CFG).exists() && !project.file(Paths.DIR_RUN).exists());
            startupMessage.exec();

            Step downloadAcpData = new DownloadFile(project, logger, "Downloading ACP data")
                    .setInput(Util.toMavenUrl(Util.getAncientMcMaven(), extension.getData().get(), "zip"))
                    .setOutput(project.file(Paths.ACP_DATA))
                    .setCondition(!project.file(Paths.ACP_DATA).exists());
            downloadAcpData.exec();

            Step extractAcpData = new ExtractFile(project, logger, "Extracting ACP data")
                    .setInput(project.file(Paths.ACP_DATA))
                    .setOutput(project.file(Paths.DIR_CFG))
                    .setCondition(!project.file(Paths.TSRG).exists());
            extractAcpData.exec();

            Step downloadVersionManifest = new DownloadFile(project, logger, "Downloading version manifest")
                    .setInput(Util.getUrl("https://raw.githubusercontent.com/ancientmc/ancientmc-maven/refs/heads/maven/net/minecraft/versions/ancientmc_manifest.json"))
                    .setOutput(project.file(Paths.VERSION_MANIFEST))
                    .setCondition(!project.file(Paths.VERSION_MANIFEST).exists());
            downloadVersionManifest.exec();

            Step downloadJson = new DownloadFile(project, logger, "Downloading version JSON")
                    .setInput(Json.getJsonUrl(project.file(Paths.VERSION_MANIFEST), version))
                    .setOutput(project.file(Paths.JSON))
                    .setCondition(!project.file(Paths.JSON).exists());
            downloadJson.exec();

            Step resolveLibraries = new ResolveLibraries(project, logger, "Resolving Minecraft libraries")
                    .setLibraries(Json.getLibraries(Arrays.asList(project.file(Paths.JSON), project.file(Paths.DIR_CFG + "jardep.json"))))
                    .setCondition(Util.areDependenciesPresent(project, "runtimeClasspath"));
            resolveLibraries.exec();

            Step resolveTools = new ResolveTools(project, logger, "Resolving ACP tools")
                    .setTools(Json.getTools(project.file(Paths.TOOLS_JSON)))
                    .setCondition(Util.areDependenciesPresent(project, "fernflower"));
            resolveTools.exec();

            Step extractNatives = new ExtractNatives(project, logger, "Extracting natives")
                    .setOutput(project.file(Paths.DIR_NATIVES))
                    .setUrls(Json.getNativeUrls(project.file(Paths.JSON)))
                    .setCondition(!project.file(Paths.DIR_NATIVES).exists());
            extractNatives.exec();

            Step downloadAssets = new DownloadAssets(project, logger, "Downloading assets")
                    .setIndexUrl(Json.getAssetIndexUrl(project.file(Paths.JSON)))
                    .setOutput(project.file(Paths.DIR_ASSETS))
                    .setCondition(Json.areAssetsPresent(project.file(Paths.JSON)) &&
                            FileUtil.isDirectoryEmpty(project.file(Paths.DIR_ASSETS)));
            downloadAssets.exec();

            Step downloadClient = new DownloadFile(project, logger, "Downloading client JAR")
                    .setInput(Json.getJarUrl(project.file(Paths.JSON), "client"))
                    .setOutput(project.file(Paths.CLIENT_JAR))
                    .setCondition(!project.file(Paths.CLIENT_JAR).exists());
            downloadClient.exec();

            Step copyStart = new CopyFile(project, logger, "Copying start files")
                    .setInput(project.file(Paths.DIR_START))
                    .setOutput(project.file(Paths.DIR_SRC + "acp/client/"))
                    .setCondition(!project.file(Paths.DIR_SRC + "acp/client/Start.java").exists());
            copyStart.exec();
        } catch (IOException e) {
            throw new AcpException("Initialization error.", logger, e);
        }
    }
}
