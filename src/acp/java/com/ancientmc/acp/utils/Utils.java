package com.ancientmc.acp.utils;

import com.ancientmc.acp.ACPExtension;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.srgutils.IMappingFile;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.internal.os.OperatingSystem;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class full of miscellaneous utilities.
 */
public class Utils {

    /**
     * Useful utility method for easily converting a JSON file into a JSON object parsable by GSON.
     * @param json The JSON file.
     * @return The JSON file as a GSON object.
     * @throws IOException
     */
    public static JsonObject getJsonAsObject(File json) throws IOException {
        Reader reader = Files.newBufferedReader(json.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        return element.getAsJsonObject();
    }

    /**
     * Gets a map of class names from the SRG file. The key is the obfuscated name, while the value is the mapped name.
     * @param srg The SRG file.
     * @return The class map.
     * @throws IOException
     */
    public static Map<String, String> getClassMap(File srg) throws IOException {
        Map<String, String> map = new HashMap<>();
        IMappingFile mapping = IMappingFile.load(srg);
        mapping.getClasses().forEach(cls -> map.put(cls.getOriginal(), cls.getMapped()));
        return map;
    }

    /**
     * Converts a maven path into a URL whose contents can be downloaded.
     * @param repo The repository URL.
     * @param path The maven path (group.sub:name:version).
     * @param ext The file extension.
     * @return The maven URL.
     */
    public static URL toMavenUrl(String repo, String path, String ext) {
        try {
            String[] split = path.split(":");
            String file = split[1] + "-" + split[2] + (split.length > 3 ? "-" + split[3] : "") + "." + ext;
            String newPath = split[0].replace('.', '/') + "/" + split[1] + "/" + split[2] + "/" + file;
            return new URL(repo + newPath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Simple compression function for multiple files.
     * @param files The files.
     * @param zip The output ZIP.
     * @throws IOException
     */
    public static void compress(Collection<File> files, File zip) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zip));
        for(File file : files) {
            zipOut.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream in = new FileInputStream(file);
            int len;
            byte[] b = new byte[4096];

            while ((len = in.read(b)) > 0) {
                zipOut.write(b, 0, len);
            }
            zipOut.closeEntry();
            in.close();
        }
        zipOut.close();
    }

    /**
     * Gets a shortened version of the operating system's name. This class is used in getting the native URLs,
     * as different versions for LWJGL's natives are needed depending on the operating system.
     * @see Json#getNativeUrls(File)
     */
    public static String getOSName() {
        OperatingSystem os = OperatingSystem.current();
        if(os.isWindows()) {
            return "windows";
        } else if (os.isMacOsX()) {
            return "osx";
        } else if (os.isLinux() || os.isUnix()) {
            return "linux";
        }
        return "unknown";
    }

    public static String getAncientMCMaven() {
        return "https://github.com/ancientmc/ancientmc-maven/raw/maven/";
    }
}
