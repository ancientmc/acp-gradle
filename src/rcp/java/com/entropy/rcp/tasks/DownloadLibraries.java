/**
 * Task: downloadLibraries
 * Function: Downloads Minecraft's dependencies into a repository generated in the gradle cache.
 * This repository is called as a flatDir in the main build.gradle.
 */

package com.entropy.rcp.tasks;

import com.entropy.rcp.utils.Paths;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;

public abstract class DownloadLibraries extends DefaultTask {

    @TaskAction
    public void downloadLibraries() throws IOException {
        File json = getJson().get().getAsFile();
        parseJson(json);
    }

    @InputFile
    public abstract RegularFileProperty getJson();

    @OutputDirectory
    public abstract RegularFileProperty getRepository();

    public void parseJson(File file) throws IOException {
        JsonObject jsonFile;
        Reader reader = Files.newBufferedReader(file.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        jsonFile = element.getAsJsonObject();
        getLibrary(jsonFile);
    }

    public void getLibrary(JsonObject object) throws IOException {
        File repository = getRepository().get().getAsFile();
        String lwjglMac = "lwjgl-2.9.1-nightly";
        String lwjglOther = "lwjgl-2.9.0";
        String utilMac = "lwjgl_util-2.9.1-nightly";
        String utilOther = "lwjgl_util-2.9.0";

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
            File file = new File(repository + path);
            FileUtils.copyURLToFile(url, file);

            System.out.println("Downloading library " + path + ".");
        }
    }
}