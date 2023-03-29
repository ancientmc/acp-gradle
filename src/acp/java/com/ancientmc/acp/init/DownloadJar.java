package com.ancientmc.acp.init;

import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadJar {
    public static void download(String side, File json, File dest, Logger logger) throws IOException {
        JsonObject jsonObj = Utils.getJsonAsObject(json);
        JsonObject sideObj = jsonObj.getAsJsonObject("downloads").getAsJsonObject(side);

        String version = jsonObj.get("id").getAsString();
        URL url = new URL(sideObj.get("url").getAsString());
        File file = new File(dest, version + (side.equals("client") ? "" : "-server") + ".jar");
        if(!file.exists()) {
            logger.lifecycle("Downloading " + side + " JAR");
            FileUtils.copyURLToFile(url, file);
        }
    }
}
