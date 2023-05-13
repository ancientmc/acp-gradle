package com.ancientmc.acp.init.step;

import com.ancientmc.acp.utils.Json;
import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This step downloads the asset files. Instead of downloading the asset hashes in their pure forms, it goes the extra mile
 * and converts those hash files into the actual resource files used by the game.
 */
public class DownloadAssetsStep extends Step {
    /**
     * The URL for the index file containing a map of resource files and their hash values.
     * The URL is retrieved from a method in the Json utilities class.
     * @see Json#getAssetIndexUrl(File)
     */
    private URL index;
    /**
     * The output file containing the resources. Specifically, this is the "run" directory in the ACP workspace.
     */
    private File output;

    /**
     * Since asset downloading is more complicated, this method merely downloads the asset index file from the URL.
     * @param logger The gradle logger.
     * @param condition Boolean condition that determines if the step gets executed.
     */
    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);
        if (condition) {
            try {
                if (!output.exists()) FileUtils.forceMkdir(output);
                String path = index.getPath().substring(index.getPath().lastIndexOf('/') + 1);
                File file = new File(output, path);
                if (!file.exists()) {
                    FileUtils.copyURLToFile(index, file);
                }

                getAssets(file, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a hash map of all the assets.
     * @param index The asset index file.
     * @param output The "run" directory in the ACP workspace.
     * @throws IOException
     */
    public static void getAssets(File index, File output) throws IOException {
        JsonObject indexObj = Utils.getJsonAsObject(index);
        Map<String, String> assets = new HashMap<>();
        JsonObject objects = indexObj.getAsJsonObject("objects");
        objects.keySet().forEach(name -> {
            String hash = objects.getAsJsonObject(name).get("hash").getAsString();
            assets.put(name, hash);
        });

        downloadAssets(assets, new File(output, "resources\\"));
    }

    /**
     * Gets each asset from the hash map and sets it up for downloading.
     * Each URL of a hash representing an asset is collected and written as a new file using its proper name.
     * @param map The hash map containing the assets.
     * @param dest The "run\resources" directory path in the ACP workspace.
     * @throws IOException
     */
    public static void downloadAssets(Map<String, String> map, File dest) throws IOException {
        if(!dest.exists()) FileUtils.forceMkdir(dest);
        map.forEach((key, value) -> {
            try {
                String path = value.substring(0, 2) + '/' + value;
                String url = "https://resources.download.minecraft.net/" + path;
                File file = new File(dest, key);
                if(!file.getParentFile().exists()) {
                    FileUtils.forceMkdir(file.getParentFile());
                }
                writeToFile(new URL(url).openStream(), new FileOutputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Writes an input URL of an asset hash to a file with its proper name.
     * @param in The input asset hash URL on Minecraft's website.
     * @param out The output file in the "run\resources" directory.
     * @throws IOException
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

    public DownloadAssetsStep setIndex(URL index) {
        this.index = index;
        return this;
    }

    public DownloadAssetsStep setOutput(File output) {
        this.output = output;
        return this;
    }
}
