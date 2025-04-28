package com.ancientmc.acp.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for JSON parsing, mainly Minecraft's version JSON.
 */
public class Json {

    /**
     * Utility method for easily converting a JSON file into a JSON object parsable by Gson.
     * @param file The JSON file.
     * @return The JSON file as a Gson object.
     * @throws IOException exception.
     */
    public static JsonObject get(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return JsonParser.parseReader(reader).getAsJsonObject();
    }

    /**
     * Utility method for easily converting a JSON URL into a JSON object parsable by Gson.
     * @param url The JSON URL link.
     * @return The JSON file as a Gson object.
     * @throws IOException exception.
     */
    public static JsonObject get(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return JsonParser.parseReader(reader).getAsJsonObject();
    }

    /**
     * Gets the JSON URL for the specified version from the version manifest file.
     * @param manifest The version manifest JSON.
     * @param version The Minecraft version, specified in the ACP end-user workspace.
     * @return The URL for the JSON file on Minecraft's website.
     */
    public static URL getJsonUrl(File manifest, String version) {
        try {
            JsonObject manifestObj = get(manifest);
            JsonArray versions = manifestObj.getAsJsonArray("versions");

            for (JsonElement entry : versions.asList()) {
                JsonElement id = entry.getAsJsonObject().get("id");

                if (id.getAsString().equals(version)) {
                    return Util.getUrl(entry.getAsJsonObject().get("url").getAsString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Gets a list of the libraries that will be added as dependencies.
     * All the libraries are formatted as maven paths (group.sub:name:version).
     * @param jsons The JSON files that the libraries are parsed from. Two JSONS are parsed: Minecraft's Version JSON created by
     *              Mojang, and a jar dependencies JSON file for libraries that are stored in the Minecraft JAR file (usually sound libraries).
     * @return The list of libraries.
     * @throws IOException exception.
     */
    public static List<String> getLibraries(List<File> jsons) throws IOException {
        List<String> libraries = new ArrayList<>();

        for (File json : jsons) {
            JsonObject object = get(json);
            JsonArray libArray = object.getAsJsonArray("libraries");

            for (JsonElement entry : libArray.asList()) {
                String name = entry.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                libraries.add(name);
            }
        }

        return libraries;
    }


    /**
     * Gets a map of tools needed by ACP for its functions. The key is the gradle configuration for the tool, and the value is the tool's maven path.
     * @param json The JSON file the tools are parsed from. Stored in the ACP directory as 'gradle/tools.json'.
     * @return The map of tools.
     * @throws IOException exception.
     */
    public static Map<String, String> getTools(File json) throws IOException {
        Map<String, String> tools = new HashMap<>();
        JsonObject jsonObject = get(json);
        JsonArray toolsArray = jsonObject.getAsJsonArray("tools");

        for (JsonElement entry : toolsArray.asList()) {
            String configuration = entry.getAsJsonObject().getAsJsonPrimitive("configuration").getAsString();
            String tool = entry.getAsJsonObject().getAsJsonPrimitive("tool").getAsString();
            tools.put(configuration, tool);
        }

        return tools;
    }

    /**
     * Gets a list of the native URLs from the JSON.
     * @param json The Minecraft version JSON.
     * @return The list of URLs.
     * @throws IOException exception.
     */
    public static List<URL> getNativeUrls(File json) throws IOException {
        JsonObject jsonObj = get(json);
        JsonArray libraries = jsonObj.getAsJsonArray("libraries");
        List<URL> urls = new ArrayList<>();

        for (JsonElement entry : libraries.asList()) {
            JsonObject downloads = entry.getAsJsonObject().getAsJsonObject("downloads");

            if (downloads.has("classifiers")) {
                String os = Util.getOsName();
                JsonObject natives = downloads.getAsJsonObject("classifiers").getAsJsonObject("natives-" + os);

                if (natives != null) {
                    URL url = Util.getUrl(natives.get("url").getAsString());
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
     * @throws IOException exception.
     */
    public static URL getAssetIndexUrl(File json) throws IOException {
        JsonObject jsonObj = get(json);
        return Util.getUrl(jsonObj.getAsJsonObject("assetIndex").get("url").getAsString());
    }

    /**
     * Gets the URL for Minecraft's JAR file(s).
     * @param json The Minecraft Version JSON.
     * @param side The game side. Acceptable inputs are "client" and "server", though older versions may not have the server JAR in their
     *             JSONs.
     * @return The URL to the JAR file.
     * @throws IOException exception.
     */
    public static URL getJarUrl(File json, String side) throws IOException {
        JsonObject jsonObj = get(json);
        JsonObject sideObj = jsonObj.getAsJsonObject("downloads").getAsJsonObject(side);
        return Util.getUrl(sideObj.get("url").getAsString());
    }
}
