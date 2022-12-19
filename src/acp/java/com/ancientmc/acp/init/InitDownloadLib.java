package com.ancientmc.acp.init;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;

public class InitDownloadLib {
    private static final String lwjglVersion = "2.9.0";
    private static final String lwjglMacVersion = "2.9.1-nightly";

    public static void init(File json, File repository) throws IOException {
        JsonObject jsonFile;
        Reader reader = Files.newBufferedReader(json.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        jsonFile = element.getAsJsonObject();
        getLibrary(jsonFile, repository);
    }

    public static void getLibrary(JsonObject object, File repository) throws IOException {

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
                if(path.contains(lwjglVersion)) continue;
            } else {
                if(path.contains(lwjglMacVersion)) continue;
            }

            URL url = new URL(entry.getAsJsonObject("artifact").get("url").getAsString());
            File file = new File(repository + "/" + path);
            FileUtils.copyURLToFile(url, file);

            System.out.println("Downloading library " + path + ".");
        }
    }
}