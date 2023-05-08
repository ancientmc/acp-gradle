package com.ancientmc.acp.init.step;

import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DownloadAssetsStep extends Step {
    private File json;
    private File output;

    @Override
    public void exec() {
        try {
            if (!output.exists()) FileUtils.forceMkdir(output);

            JsonObject jsonObj = Utils.getJsonAsObject(json);
            JsonObject assetIndex = jsonObj.getAsJsonObject("assetIndex");
            String indexURL = assetIndex.get("url").getAsString();
            String path = indexURL.substring(indexURL.lastIndexOf('/') + 1);
            File file = new File(output, path);
            if(!file.exists()) {
                FileUtils.copyURLToFile(new URL(indexURL), file);
                getAssets(file, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getAssets(File index, File dest) throws IOException {
        File resources = new File(dest, "resources\\");
        if(!resources.exists()) {
            FileUtils.forceMkdir(resources);

            JsonObject indexObj = Utils.getJsonAsObject(index);
            Map<String, String> assets = new HashMap<>();
            JsonObject objects = indexObj.getAsJsonObject("objects");
            objects.keySet().forEach(name -> {
                String hash = objects.getAsJsonObject(name).get("hash").getAsString();
                assets.put(name, hash);
            });

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

    public DownloadAssetsStep setJson(File json) {
        this.json = json;
        return this;
    }

    public DownloadAssetsStep setOutput(File output) {
        this.output = output;
        return this;
    }
}
