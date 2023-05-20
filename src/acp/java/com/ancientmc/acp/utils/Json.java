package com.ancientmc.acp.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for JSON parsing, mainly Minecraft's version JSON.
 */
public class Json {
    private static final String LWJGL_VERSION = "2.9.0";
    private static final String LWJGL_MAC_VERSION = "2.9.1";

    /**
     * Gets the JSON URL for the specified version from the version manifest file.
     * @param manifest The version manifest JSON.
     * @param version The Minecraft version, specified in the ACP end-user workspace.
     * @return The URL for the JSON file on Minecraft's website.
     */
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

    /**
     * Gets a list of the libraries that will be added as dependencies.
     * All the libraries are formatted as maven paths (group.sub:name:version).
     * @param jsons The JSON files that the libraries are parsed from. Two JSONS are parsed: Minecraft's Version JSON created by
     *              Mojang, and a jar dependencies JSON file for libraries that are stored in the Minecraft JAR file (usually sound libraries).
     * @return The list of libraries.
     * @throws IOException
     */
    public static List<String> getLibraries(List<File> jsons) throws IOException {
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

    /**
     * Gets a list of the native URLs from the JSON.
     * @param json The Minecraft version JSON.
     * @return The list of URLs.
     * @throws IOException
     */
    public static List<URL> getNativeUrls(File json) throws IOException {
        JsonObject jsonObj = Utils.getJsonAsObject(json);
        JsonArray libraries = jsonObj.getAsJsonArray("libraries");
        List<URL> urls = new ArrayList<>();

        for (int i = 0; i < libraries.size(); i++) {
            String name = libraries.get(i).getAsJsonObject().get("name").getAsString();
            JsonObject entry = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
            if(entry.has("classifiers")) {
                String os = OSName.get();
                JsonObject natives = entry.getAsJsonObject("classifiers").getAsJsonObject("natives-" + os);
                if (natives != null && isAllowed(name)) {
                    URL url = new URL(natives.get("url").getAsString());
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * Gets the URL to the asset index from within the version JSON, which contains hashes that correspond to
     * Minecraft's resources (ones not already present within the JAR).
     * @param json The Minecraft version JSON.
     * @return The asset index URL.
     * @throws IOException
     */
    public static URL getAssetIndexUrl(File json) throws IOException {
        JsonObject jsonObj = Utils.getJsonAsObject(json);
        return new URL(jsonObj.getAsJsonObject("assetIndex").get("url").getAsString());
    }

    /**
     * Gets the URL for Minecraft's JAR file(s).
     * @param json The Minecraft Version JSON.
     * @param side The game side. Acceptable inputs are "client" and "server", though older versions may not have the server JAR in their
     *             JSONs.
     * @return The URL to the JAR file.
     * @throws IOException
     */
    public static URL getJarUrl(File json, String side) throws IOException {
        JsonObject jsonObj = Utils.getJsonAsObject(json);
        JsonObject sideObj = jsonObj.getAsJsonObject("downloads").getAsJsonObject(side);
        return new URL(sideObj.get("url").getAsString());
    }

    /**
     * Filters through the correct LWJGL version to download. All libraries are passed through this method in above methods, but any non-LWJGL library
     * will get skipped through the first if statement.
     */
    public static boolean isAllowed(String name) {
        if (!name.contains("org.lwjgl")) return true;
        if (OperatingSystem.current().isMacOsX()) {
            return name.contains(LWJGL_MAC_VERSION);
        } else return name.contains(LWJGL_VERSION);
    }
}
