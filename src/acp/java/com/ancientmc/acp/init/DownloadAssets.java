package com.ancientmc.acp.init;

import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DownloadAssets {
    public static void exec(File json, File dest, Logger logger) throws IOException {
        if (!dest.exists()) FileUtils.forceMkdir(dest);

        JsonObject jsonObj = Utils.getJsonAsObject(json);
        JsonObject assetIndex = jsonObj.getAsJsonObject("assetIndex");
        String indexURL = assetIndex.get("url").getAsString();
        String path = indexURL.substring(indexURL.lastIndexOf('/') + 1);
        File file = new File(dest, path);
        if(!file.exists()) FileUtils.copyURLToFile(new URL(indexURL), file);
        getAssets(file, dest, logger);
    }

    public static void getAssets(File index, File dest, Logger logger) throws IOException {
        JsonObject indexObj = Utils.getJsonAsObject(index);
        Map<String, String> assets = new HashMap<>();
        JsonObject objects = indexObj.getAsJsonObject("objects");
        objects.keySet().forEach(name -> {
            String hash = objects.getAsJsonObject(name).get("hash").getAsString();
            assets.put(name, hash);
        });

        File resources = new File(dest, "resources\\");
        if(!resources.exists()) {
            logger.lifecycle("Downloading assets");
            FileUtils.forceMkdir(resources);
        }

        assets.forEach((key, value) -> {
            try {
                String path = value.substring(0, 2) + '/' + value;
                String url = "https://resources.download.minecraft.net/" + path;
                File file = new File(resources, key);
                if(!file.getParentFile().exists()) {
                    FileUtils.forceMkdir(file.getParentFile());
                }
                writeToFile(new URL(url).openStream(), new FileOutputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

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
}
