package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.Json;
import com.ancientmc.acp.util.Util;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * This step downloads the asset files. Instead of downloading the asset hashes in their pure forms, it goes the extra mile
 * and converts those hash files into the actual resource files used by the game.
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

    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        if (condition) {
            try {
                if (!output.exists()) {
                    FileUtils.forceMkdir(output);
                }

                JsonObject indexObj = Json.get(index);
                Map<String, String> assets = getAssets(indexObj);

                if (assets != null) {
                    download(assets, output);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Retrieves a hash map of all the assets.
     * @param index The asset index JSON object.
     * @return The assets as a map. The key is the name of the asset, while the value is its hash. Returns null if the index is empty.
     * @throws IOException exception.
     */
    public static Map<String, String> getAssets(JsonObject index) throws IOException {
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
     * Gets each asset from the hash map and sets it up for downloading.
     * Each URL of a hash representing an asset is collected and written as a new file using its proper name.
     * @param map The hash map containing the assets.
     * @param dest The "run\resources" directory path in the ACP workspace.
     * @throws IOException exception.
     */
    public static void download(Map<String, String> map, File dest) throws IOException {
        if(!dest.exists()) {
            FileUtils.forceMkdir(dest);
        }

        map.forEach((key, value) -> {
            try {
                String path = value.substring(0, 2) + '/' + value;
                URL url = Util.getUrl("https://resources.download.minecraft.net/" + path);
                File file = new File(dest, key);

                if(!file.getParentFile().exists()) {
                    FileUtils.forceMkdir(file.getParentFile());
                }
                writeToFile(url.openStream(), Files.newOutputStream(file.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Writes an input URL of an asset hash to a file with its proper name.
     * @param in The input asset hash URL on Minecraft's website.
     * @param out The output file in the "run\resources" directory.
     * @throws IOException exception.
     */
    public static void writeToFile(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[1024];
        int len;

        while ((len = in.read(b)) > 0) {
            out.write(b, 0, len);
            out.flush();
        }
        in.close();
        out.close();
    }

    public File getOutput() {
        return output;
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
