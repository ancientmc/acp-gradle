package com.ancientmc.acp.task.step;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.FileUtil;
import com.ancientmc.acp.util.Json;
import com.ancientmc.acp.util.Util;
import com.google.gson.JsonObject;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * This step downloads the asset files. Instead of downloading the asset hashes in their pure forms, it goes the extra mile
 * and converts those hash files into the actual resource files used by the game.
 * @author moist-mason
 */
public class DownloadAssets extends Step {

    /**
     * The URL for the index file containing a map of resource files and their hash values.
     * The URL is retrieved from a method in the Json utilities class.
     * @see Json#getAssetIndexUrl(File)
     */
    private URL indexUrl;

    /**
     * The output file containing the resources: "run/resources" in the ACP workspace.
     */
    private File output;

    public DownloadAssets(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        FileUtil.createDirectory(output);

        logger.file(project, "Json index -> {}", indexUrl.toString());
        JsonObject index = Json.get(indexUrl);
        List<Asset> assets = getAssets(index);

        if (!assets.isEmpty()) {
            logger.file(project, "Asset size -> {}", Integer.toString(assets.size()));
            download(assets, output);
        }
    }

    public List<Asset> getAssets(JsonObject index) {
        List<Asset> assets = new ArrayList<>();
        JsonObject objects = index.getAsJsonObject("objects");

        if (objects.isEmpty()) {
            return Collections.emptyList();
        }

        objects.keySet().forEach(name -> {
            String hash = objects.getAsJsonObject(name).get("hash").getAsString();
            boolean omniArchive = objects.getAsJsonObject(name).has("url");
            assets.add(new Asset(name, hash, omniArchive));
        });

        return assets;
    }

    public void download(List<Asset> assets, File directory) throws IOException {
        FileUtil.createDirectory(directory);

        for (Asset asset : assets) {
            URL url = asset.getUrl();
            File file = new File(directory, asset.name);
            FileUtil.download(logger, url, file);
        }
    }

    public DownloadAssets setIndexUrl(URL indexUrl) {
        this.indexUrl = indexUrl;
        return this;
    }

    public DownloadAssets setOutput(File output) {
        this.output = output;
        return this;
    }

    /**
     * Object representation of a Minecraft asset.
     * @param name The asset file name, as it is called by Minecraft.
     * @param hash The hash that is stored on Minecraft's website.
     * @param omniArchive Determines if the asset needs to be downloaded from OmniArchive's website or from Mojang's.
     */
    public record Asset(String name, String hash, boolean omniArchive) {

        /**
         * @return the URL of this asset's hash.
         */
        public URL getUrl() {
            String path = hash.substring(0, 2) + '/' + hash;
            String domain = omniArchive ? "https://meta.omniarchive.uk/resources/" : "https://resources.download.minecraft.net/";
            return Util.getUrl(domain + path);
        }
    }
}