package com.ancientmc.acp.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

public class Utils {
    public static JsonObject getJsonAsObject(File json) throws IOException {
        Reader reader = Files.newBufferedReader(json.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        return element.getAsJsonObject();
    }

    public static String toMavenUrl(String repo, String path, String ext) {
        String[] split = path.split(":");
        String file = split[1] + "-" + split[2] + (split.length > 3 ? "-" + split[3] : "") + "." + ext;
        String newPath = split[0].replace('.', '/') + "/" + split[1] + "/" + split[2] + "/" + file;
        return repo + newPath;
    }

    public static String getAncientMCMaven() {
        return "https://github.com/ancientmc/ancientmc-maven/raw/maven/";
    }
}
