package com.ancientmc.acp.init;

import com.ancientmc.acp.ACPExtension;
import com.ancientmc.acp.utils.Paths;
import com.ancientmc.acp.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ACPInitialization {
    private static final String maven = Utils.getAncientMCMaven();

    // TODO: Make this nicer.
    public static void init(Project proj, ACPExtension extension, String version) throws IOException {
        Logger logger = proj.getLogger();

        // Download ACP data
        String data = extension.getData().get();
        File dataZip = new File(Paths.DIR_CFG + "\\data.zip");
        if (!dataZip.exists()) {
            logger.lifecycle("Initializing ACP environment");
            logger.lifecycle("Downloading ACP data");
            FileUtils.copyURLToFile(new URL(Utils.toMavenUrl(maven, data, "zip")), dataZip);
            logger.lifecycle("Extracting ACP data");
        }

        // Extract ACP data into cfg directory
        proj.copy(action -> {
            action.from(proj.zipTree(dataZip));
            action.into(proj.file(Paths.DIR_CFG));
        });

        // Download version manifest
        URL manifestURL = new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
        File manifest = proj.file(Paths.DIR_TEMP + "version_manifest.json");
        if(!manifest.exists()) {
            logger.lifecycle("Downloading version manifest");
            FileUtils.copyURLToFile(manifestURL, manifest);
        }

        // Download version JSON
        File json = proj.file(Paths.DIR_TEMP + version + ".json");
        File jarDepJson = proj.file(Paths.DIR_CFG + "jardep.json");
        File[] jsons = { json, jarDepJson };
        if(!json.exists()) {
            logger.lifecycle("Downloading version JSON");
            DownloadJson.download(manifest, json, version);
        }

        // Add libraries as resolved dependencies
        List<String> libraries = GetLibraries.getLibraries(jsons);
        DependencySet deps = proj.getConfigurations().getByName("implementation").getDependencies();
        proj.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            public void beforeResolve(ResolvableDependencies resolvableDependencies) {
                for(String library : libraries) {
                    deps.add(proj.getDependencies().create(library));
                }
                proj.getGradle().removeListener(this);
            }

            @Override
            public void afterResolve(ResolvableDependencies resolvableDependencies) { }
        });

        // Extract natives
        File nativesDir = proj.file(Paths.DIR_NATIVES);
        if (!nativesDir.exists()) {
            logger.lifecycle("Extracting natives");
            FileUtils.forceMkdir(nativesDir);
        }
        ExtractNatives.extract(json, nativesDir, proj);

        // Download assets
        File runDir = proj.file(Paths.DIR_RUN);
        DownloadAssets.exec(json, runDir, logger);

        // Download Minecraft JAR(s)
        DownloadJar.download("client", json, new File(Paths.DIR_TEMP), logger);
    }
}
