package com.ancientmc.modtools.tasks;

import com.ancientmc.acp.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class DownloadModLoader extends DefaultTask {

    @TaskAction
    void exec() {
        try {
            String version = getVersion().get();
            String loader = getModLoader().get();
            File output = getOutputDir().get().getAsFile();

            String repo = Utils.getAncientMCMaven();

            URL url = getURL(repo, version, loader);

            if (!output.exists()) {
                FileUtils.forceMkdir(output);
            }
            FileUtils.copyURLToFile(url, new File(output, "modloader.lzma"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private URL getURL(String repo, String version, String loader) throws MalformedURLException {
        String ml = getModLoaderPath(loader);

        String mavenPath = ml + ":" + version;
        String urlPath = Utils.toMavenUrl(repo, mavenPath, "lzma");
        return new URL(urlPath);
    }

    private String getModLoaderPath(String type) {
        if (type.equals("forge")) {
            return "net.minecraftforge:forge";
        } else if (type.equals("risugami")) {
            return "risugami:modloader";
        } else {
            getLogger().error("Unrecognized mod loader: " + type);
            return null;
        }
    }

    @Input
    public abstract Property<String> getVersion();

    @Input
    public abstract Property<String> getModLoader();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();
}
