package com.entropy.rcp.init;

import com.entropy.rcp.utils.Paths;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;

public class InitDownloadLib {

    public static void init(String urlPath, File json, File repository) throws IOException {
        URL url = new URL(urlPath);
        FileUtils.copyURLToFile(url, json);
        init(json, repository);
    }

    public static void init(File json, File repository) throws IOException {
        JsonObject jsonFile;
        Reader reader = Files.newBufferedReader(json.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        jsonFile = element.getAsJsonObject();
        getLibrary(jsonFile, repository);
    }

    public static void getLibrary(JsonObject object, File repository) throws IOException {
        String lwjglMac = "lwjgl-2.9.1-nightly";
        String lwjglOther = "lwjgl-2.9.0";
        String utilMac = "lwjgl_util-2.9.1-nightly";
        String utilOther = "lwjgl_util-2.9.0";

        if(!repository.exists()) {
            FileUtils.forceMkdir(repository);
        }

        JsonArray libraries = object.getAsJsonObject().getAsJsonArray("libraries");
        for (int i = 0; i < libraries.size(); i++) {
            JsonObject entry = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
            if (entry.has("classifiers")) {
                continue;
            }
            String path = entry.getAsJsonObject("artifact").get("path").getAsString();

            if(OperatingSystem.current().isMacOsX()) {
                if(path.contains(lwjglOther) || path.contains(utilOther)) continue;
            } else {
                if(path.contains(lwjglMac) || path.contains(utilMac)) continue;
            }

            URL url = new URL(entry.getAsJsonObject("artifact").get("url").getAsString());
            File file = new File(repository + "/" + path);
            FileUtils.copyURLToFile(url, file);

            System.out.println("Downloading library " + path + ".");
        }
    }
}