package com.ancientmc.acp.init;

import com.ancientmc.acp.utils.OSName;
import com.ancientmc.acp.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExtractNatives {
    private static final String lwjglVersion = "2.9.0";
    private static final String lwjglMacVersion = "2.9.1-nightly";

    public static void extract(File json, File dest, Project project) throws IOException {

        List<File> jars = new ArrayList<>();
        JsonObject object = Utils.getJsonAsObject(json);
        JsonArray libraries = object.getAsJsonArray("libraries");

        for (int i = 0; i < libraries.size(); i++) {
            JsonObject entry = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
            if (!entry.has("classifiers")) continue;
            String os = OSName.getOSName();

            JsonObject natives = entry.getAsJsonObject("classifiers").getAsJsonObject("natives-" + os);
            if (natives == null) continue;
            String path = natives.get("path").getAsString();
            URL url = new URL(natives.get("url").getAsString());

            if (OperatingSystem.current().isMacOsX()) {
                if (path.contains(lwjglVersion)) continue;
            } else {
                if (path.contains(lwjglMacVersion)) continue;
            }
            File file = new File(dest, path.substring(path.lastIndexOf('/') + 1));
            FileUtils.copyURLToFile(url, file);
            jars.add(file);
        }

        jars = jars.stream().distinct().collect(Collectors.toList());
        jars.forEach(jar -> {
            project.copy(action -> {
                action.from(project.zipTree(jar));
                action.into(dest);
            });
        });
    }
}
