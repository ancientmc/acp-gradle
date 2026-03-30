package com.ancientmc.acp.task;

import com.ancientmc.acp.util.AcpException;
import com.ancientmc.acp.util.FileUtil;
import com.ancientmc.acp.util.Util;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Downloads the LZMA for the ModLoader into a specified folder. The LZMA will get injected later via the
 * injectModPatches step during setup.
 * @author moist-mason
 */
public abstract class DownloadModLoader extends AcpTask {

    @Override
    public void action(final Project project) {
        try {
            String version = getVersion().get();
            String loader = getModLoader().get();
            File directory = getOutputDir().getAsFile().get();
            String repo = Util.getAncientMcMaven();

            URL url = getURL(repo, version, loader);
            File output = project.file(directory.getAbsolutePath() + "modloader.lzma");
            FileUtil.download(logger, url, output);
        } catch (IOException e) {
            throw new AcpException("Modloader download error.", logger, e);
        }
    }

    /**
     * Gets the URL for the ModLoader LZMA in the AncientMC repo.
     * @param repo The URL for the AncientMC repo.
     * @param version The Minecraft version.
     * @param loader The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft
     * Forge).
     * @return The URL for the ModLoader LZMA.
     */
    private URL getURL(String repo, String version, String loader) {
        String ml = getModLoaderPath(loader);
        String mavenPath = ml + ":" + version;
        return Util.toMavenUrl(repo, mavenPath, "lzma");
    }

    /**
     * Gets the ModLoader maven path.
     * @param loader The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft
     * Forge).
     * @return The maven path.
     */
    private String getModLoaderPath(String loader) {
        return switch (loader) {
            case "forge" -> "net.minecraftforge:forge";
            case "risugami" -> "risugami:modloader";
            default -> throw new AcpException("Unrecognized mod loader: " + loader, logger, new IllegalArgumentException());
        };
    }

    /** The Minecraft version. */
    @Input
    public abstract Property<String> getVersion();

    /** The ModLoader type. Acceptable options are "risugami" (Risugami's ModLoader) or "forge" (Minecraft Forge). */
    @Input
    public abstract Property<String> getModLoader();

    /** The output directory for LZMA mod patches (cfg\modpatches). */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();
}