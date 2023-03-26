package com.ancientmc.acp.init;

import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetLibraries {
    public static List<String> libraryList = new ArrayList<>();
    private static final String lwjglVersion = "2.9.0";
    private static final String lwjglMacVersion = "2.9.1-nightly";

    public static List<String> getLibraries(File[] jsons) throws IOException {
        for(File json : jsons) {
            JsonObject object = Utils.getJsonAsObject(json);
            JsonArray libraries = object.getAsJsonArray("libraries");
            for(int i = 0; i < libraries.size(); i++) {
                JsonObject entry = libraries.get(i).getAsJsonObject();
                String name = entry.getAsJsonPrimitive("name").getAsString();
                if(!name.startsWith("net.minecraft:launchwrapper")) {
                    if(OperatingSystem.current().isMacOsX()) {
                        if(name.contains(lwjglVersion)) continue;
                    } else {
                        if(name.contains(lwjglMacVersion)) continue;
                    }
                    libraryList.add(name);
                }
            }
        }

        return libraryList;
    }
}
