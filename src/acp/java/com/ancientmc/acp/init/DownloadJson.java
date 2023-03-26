package com.ancientmc.acp.init;

import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadJson {
    public static void download(File manifest, File json, String version) throws IOException {
        JsonObject manifestObject = Utils.getJsonAsObject(manifest);
        JsonArray versions = manifestObject.getAsJsonArray("versions");
        for(int i = 0; i < versions.size(); i++) {
            JsonElement id = versions.get(i).getAsJsonObject().get("id");
            if(id.getAsString().equals(version)) {
                URL url = new URL(versions.get(i).getAsJsonObject().get("url").getAsString());
                FileUtils.copyURLToFile(url, json);
            }
        }
    }
}
