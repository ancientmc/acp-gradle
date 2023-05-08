package com.ancientmc.acp.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Json {
    private static final String LWJGL_VERSION = "2.9.0";
    private static final String LWJGL_MAC_VERSION = "2.9.1";

    public static URL getJsonUrl(File manifest, String version) {
        try {
            JsonObject manifestObj = Utils.getJsonAsObject(manifest);
            JsonArray versions = manifestObj.getAsJsonArray("versions");

            for (int i = 0; i < versions.size(); i++) {
                JsonElement id = versions.get(i).getAsJsonObject().get("id");
                if(id.getAsString().equals(version)) {
                    return new URL(versions.get(i).getAsJsonObject().get("url").getAsString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getLibraries(File... jsons) throws IOException {
        List<String> libList = new ArrayList<>();

        for(File json : jsons) {
            JsonObject object = Utils.getJsonAsObject(json);
            JsonArray libraries = object.getAsJsonArray("libraries");

            for(int i = 0; i < libraries.size(); i++) {
                JsonObject entry = libraries.get(i).getAsJsonObject();
                String name = entry.getAsJsonPrimitive("name").getAsString();
                if(!name.startsWith("net.minecraft:launchwrapper") && isAllowed(name)) {
                    libList.add(name);
                }
            }
        }
        return libList;
    }

    public static List<URL> getNativeUrls(File json) throws IOException {
        JsonObject jsonObj = Utils.getJsonAsObject(json);
        JsonArray libraries = jsonObj.getAsJsonArray("libraries");
        List<URL> urls = new ArrayList<>();

        for (int i = 0; i < libraries.size(); i++) {
            String name = libraries.get(i).getAsJsonObject().get("name").getAsString();
            JsonObject entry = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
            if(entry.has("classifiers")) {
                String os = OSName.getOSName();
                JsonObject natives = entry.getAsJsonObject("classifiers").getAsJsonObject("natives-" + os);
                if (natives != null && isAllowed(name)) {
                    URL url = new URL(natives.get("url").getAsString());
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    public static URL getJarUrl(File json, String side) throws IOException {
        JsonObject jsonObj = Utils.getJsonAsObject(json);
        JsonObject sideObj = jsonObj.getAsJsonObject("downloads").getAsJsonObject(side);
        return new URL(sideObj.get("url").getAsString());
    }

    public static boolean isAllowed(String name) {
        if (OperatingSystem.current().isMacOsX()) {
            return name.contains(LWJGL_MAC_VERSION);
        } else return name.contains(LWJGL_VERSION);
    }
}
