package com.ancientmc.acp.task;

import com.ancientmc.acp.util.AcpException;
import com.ancientmc.acp.util.Util;
import org.apache.commons.io.FileUtils;
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
 * @author moist-mason
 */
public abstract class DownloadModLoader extends AcpTask {

    @TaskAction
    public void exec() {
        setLogger();

        try {
            String version = getVersion().get();
            String loader = getModLoader().get();
            File directory = getOutputDir().getAsFile().get();
            String repo = Util.getAncientMcMaven();

            if (!directory.exists()) {
                FileUtils.forceMkdir(directory);
            }

            URL url = getURL(repo, version, loader);
            File output = new File(directory, "modloader.lzma");
            logger.functions().urlToFile(url, output);
            FileUtils.copyURLToFile(url, output);
        } catch (IOException e) {
            throw new AcpException(e.getMessage(), logger, getProject(), e);
        }
    }

    /**
     * Gets the URL for the ModLoader LZMA in the AncientMC repo.
     * @param repo The URL for the AncientMC repo.
     * @param version The Minecraft version.
     * @param loader The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft Forge).
     * @return The URL for the ModLoader LZMA.
     * @throws MalformedURLException exception.
     */
    private URL getURL(String repo, String version, String loader) throws IOException {
        String ml = getModLoaderPath(loader);
        String mavenPath = ml + ":" + version;
        return Util.toMavenUrl(repo, mavenPath, "lzma");
    }

    /**
     * Gets the ModLoader maven path.
     * @param loader The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft Forge).
     * @return The maven path.
     */
    private String getModLoaderPath(String loader) {
        return switch (loader) {
            case "forge" -> "net.minecraftforge:forge";
            case "risugami" -> "risugami:modloader";
            default -> nullAndError(loader);
        };
    }


    /**
     * Prints an error and returns null if the ModLoader type is not acceptable.
     * @param loader The ModLoader type.
     * @return null.
     */
    private String nullAndError(String loader) {
        getLogger().error("Unrecognized mod loader: {}", loader);
        return null;
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