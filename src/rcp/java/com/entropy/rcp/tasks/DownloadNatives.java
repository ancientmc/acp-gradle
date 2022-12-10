package com.entropy.rcp.tasks;

import com.entropy.rcp.utils.OSName;
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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;

public abstract class DownloadNatives extends DefaultTask {
    @TaskAction
    public void downloadNatives() throws IOException {
        File json = getJson().get().getAsFile();
        parseJson(json);
    }

    public void parseJson(File file) throws IOException {
        JsonObject jsonFile;
        Reader reader = Files.newBufferedReader(file.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        jsonFile = element.getAsJsonObject();
        getNatives(jsonFile);
    }

    public void getNatives(JsonObject object) throws IOException {
        File nativesDir = getNativesDir().getAsFile().get();
        JsonArray natives = object.getAsJsonObject().getAsJsonArray("natives");
        for(int i = 0; i < natives.size(); i++) {
            JsonObject entry = natives.get(i).getAsJsonObject().getAsJsonObject("downloads");
            String osNatives = entry.get(OSName.getOSName()).getAsString();

            URL url = new URL(osNatives);
            File path = new File(nativesDir, (osNatives.substring(osNatives.lastIndexOf('/') + 1, osNatives.length())));
            FileUtils.copyURLToFile(url, path);
        }
    }

    @InputFile
    public abstract RegularFileProperty getJson();

    @OutputDirectory
    public abstract RegularFileProperty getNativesDir();
}
