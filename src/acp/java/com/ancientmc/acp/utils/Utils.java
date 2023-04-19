package com.ancientmc.acp.utils;

import com.ancientmc.acp.ACPExtension;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.srgutils.IMappingFile;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
    public static JsonObject getJsonAsObject(File json) throws IOException {
        Reader reader = Files.newBufferedReader(json.toPath());
        JsonElement element = JsonParser.parseReader(reader);
        return element.getAsJsonObject();
    }

    public static Map<String, String> getClassMap(File srg) throws IOException {
        Map<String, String> map = new HashMap<>();
        IMappingFile mapping = IMappingFile.load(srg);
        mapping.getClasses().forEach(cls -> {
            map.put(cls.getOriginal(), cls.getMapped());
        });
        return map;
    }

    public static String toMavenUrl(String repo, String path, String ext) {
        String[] split = path.split(":");
        String file = split[1] + "-" + split[2] + (split.length > 3 ? "-" + split[3] : "") + "." + ext;
        String newPath = split[0].replace('.', '/') + "/" + split[1] + "/" + split[2] + "/" + file;
        return repo + newPath;
    }

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

    public static String getAncientMCMaven() {
        return "https://github.com/ancientmc/ancientmc-maven/raw/maven/";
    }
}
