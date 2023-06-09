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

/**
 * Downloads the LZMA for the ModLoader into a specified folder. The LZMA will get injected later
 * via the injectModPatches task during setup.
 */
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

    /**
     * Gets the URL for the ModLoader LZMA in the AncientMC repo.
     * @param repo The URL for the AncientMC repo.
     * @param version The Minecraft version.
     * @param loader The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft Forge).
     * @return The URL for the ModLoader LZMA.
     * @throws MalformedURLException
     */
    private URL getURL(String repo, String version, String loader) throws MalformedURLException {
        String ml = getModLoaderPath(loader);

        String mavenPath = ml + ":" + version;
        String urlPath = Utils.toMavenUrl(repo, mavenPath, "lzma");
        return new URL(urlPath);
    }

    /**
     * Gets the ModLoader maven path.
     * @param loader The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft Forge).
     * @return The maven path.
     */
    private String getModLoaderPath(String loader) {
        if (loader.equals("forge")) {
            return "net.minecraftforge:forge";
        } else if (loader.equals("risugami")) {
            return "risugami:modloader";
        } else {
            getLogger().error("Unrecognized mod loader: " + loader);
            return null;
        }
    }

    /**
     * The Minecraft version.
     */
    @Input
    public abstract Property<String> getVersion();

    /**
     * The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft Forge).
     */
    @Input
    public abstract Property<String> getModLoader();

    /**
     * The output directory for LZMA mod patches (cfg\modpatches).
     */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();
}
