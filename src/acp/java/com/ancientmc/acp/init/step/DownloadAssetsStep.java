package com.ancientmc.acp.init.step;

import com.ancientmc.acp.utils.Paths;
import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DownloadAssetsStep extends Step {
    private URL index;
    private File output;

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
