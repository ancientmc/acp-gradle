package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.AcpException;
import com.ancientmc.acp.util.FileUtil;
import com.ancientmc.acp.util.Json;
import com.ancientmc.acp.util.Util;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private URL index;

    /**
     * The output file containing the resources: "run/resources" in the ACP workspace.
     */
    private File output;

    public DownloadAssets(Project project, AcpLogger logger, String message) {
        build(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        FileUtil.createDirectory(output);

        logger.file(project, "Json index -> {}", index.toString());
        JsonObject indexObj = Json.get(index);
        Map<String, String> assets = getAssets(indexObj);
        List<String> omniArchiveAssets = getOmniArchiveAssets(indexObj);

        if (assets != null && omniArchiveAssets != null) {
            logger.file(project, "Asset size -> {}", Integer.toString(assets.size() + omniArchiveAssets.size()));
            download(assets, omniArchiveAssets, output);
        }
    }

    /**
     * Retrieves a hash map of all the assets.
     * @param index The asset index JSON object.
     * @return The assets as a map. The key is the name of the asset, while the value is its hash. Returns null if the index is empty.
     */
    public Map<String, String> getAssets(JsonObject index) {
        Map<String, String> assets = new HashMap<>();
        JsonObject objects = index.getAsJsonObject("objects");

        if (!objects.isEmpty()) {
            objects.keySet().forEach(name -> {
                String hash = objects.getAsJsonObject(name).get("hash").getAsString();
                assets.put(name, hash);
            });

            return assets;
        }

        return null;
    }

    /**
     * Some of the assets are *not* on Mojang's website but are in OmniArchive instead. This method retrieves them as a list,
     * and we later call them as needed.
     * @param index The asset index JSON object.
     * @return The list of resources only found on OmniArchive.
     */
    public List<String> getOmniArchiveAssets(JsonObject index) {
        List<String> assets = new ArrayList<>();
        JsonObject objects = index.getAsJsonObject("objects");

        if (!objects.isEmpty()) {
            objects.keySet().forEach(name -> {
                String hash = objects.getAsJsonObject(name).get("hash").getAsString();

                // only the OmniArchive-exclusive assets have a URL property.
                if (objects.getAsJsonObject(name).has("url")) {
                    assets.add(hash);
                }
            });

            return assets;
        }

        return null;
    }

    /**
     * Gets each asset from the hash map and sets it up for downloading.
     * Each URL of a hash representing an asset is collected and written as a new file using its proper name.
     * @param map The hash map containing the assets.
     * @param omniArchiveAssets The list containing the hashes only found on OmniArchive.
     * @param directory The "run\resources" directory path in the ACP workspace.
     * @throws IOException exception.
     */
    public void download(Map<String, String> map, List<String> omniArchiveAssets, File directory) throws IOException {
        if(!directory.exists()) {
            FileUtils.forceMkdir(directory);
        }

        map.forEach((key, value) -> {
            try {
                String path = value.substring(0, 2) + '/' + value;
                String domain = omniArchiveAssets.contains(value) ? "https://meta.omniarchive.uk/resources/" : "https://resources.download.minecraft.net/";
                URL url = Util.getUrl(domain + path);
                File file = new File(directory, key);

                FileUtil.createDirectory(file.getParentFile());

                logger.functions().urlToFile(url, file);
                writeToFile(url.openStream(), Files.newOutputStream(file.toPath()));
            } catch (IOException e) {
                throw new AcpException(e.getMessage(), logger, project, e);
            }
        });
    }

    /**
     * Writes an input URL of an asset hash to a file with its proper name.
     * @param in The input asset hash URL on Minecraft's website.
     * @param out The output file in the "run\resources" directory.
     * @throws IOException exception.
     */
    public void writeToFile(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[1024];
        int len;

        while ((len = in.read(b)) > 0) {
            out.write(b, 0, len);
            out.flush();
        }

        in.close();
        out.close();
    }

    public DownloadAssets setIndex(URL index) {
        this.index = index;
        return this;
    }

    public DownloadAssets setOutput(File output) {
        this.output = output;
        return this;
    }
}
