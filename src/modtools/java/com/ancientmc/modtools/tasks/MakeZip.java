package com.ancientmc.modtools.tasks;

import com.ancientmc.acp.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public abstract class MakeZip extends DefaultTask {
    @TaskAction
    public void exec() {
        // TODO: simplify inputs somehow? Five is a lot.
        File originalHash = getOriginalHash().get().getAsFile();
        File moddedHash = getModdedHash().get().getAsFile();
        File srg = getSrg().get().getAsFile();
        File obfDirectory = getObfuscatedClassDirectory().get().getAsFile();
        File zip = getZip().get().getAsFile();

        try {
            // Retrieve hash maps.
            Map<String, String> originalMap = getHashMap(originalHash);
            Map<String, String> moddedMap = getHashMap(moddedHash);
            Map<String, String> classMap = Utils.getClassMap(srg);

            // Remove ACP start class from map.
            moddedMap.remove("acp/client/Start");

            List<File> moddedClasses = new ArrayList<>();
            moddedMap.forEach((name, hash) -> {
                if (!originalMap.containsValue(hash)) {
                    // Get the class file names without packages.
                    String strippedName = name.substring(name.lastIndexOf('/') + 1);

                    String className = classMap.containsValue(name) ? getObfName(name, classMap) : strippedName;
                    File moddedClass = new File(obfDirectory, className + ".class");
                    moddedClasses.add(moddedClass);
                }
            });

            Utils.compress(moddedClasses, zip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a map of the text file containing the hashes.
     * The key is the class name, while the value is the hash.
     */
    public static Map<String, String> getHashMap(File hashFile) throws IOException {
        Map<String, String> map = new HashMap<>();
        List<String> lines = FileUtils.readLines(hashFile, StandardCharsets.UTF_8);
        lines.forEach(line -> {
            String[] split = line.split(" ");

            // split[0] = class name; split[1] = hash
            map.put(split[0], split[1]);
        });
        return map;
    }

    /**
     * Get obfuscated class name.
     */
    public String getObfName(String name, Map<String, String> map) {
        return map.entrySet().stream()
                .filter(entry -> name.equals(entry.getValue()))
                .map(Map.Entry::getKey).findAny().get();
    }

    @InputDirectory
    public abstract DirectoryProperty getObfuscatedClassDirectory();

    @InputFile
    public abstract RegularFileProperty getOriginalHash();

    @InputFile
    public abstract RegularFileProperty getModdedHash();

    @InputFile
    public abstract RegularFileProperty getSrg();

    @OutputFile
    public abstract RegularFileProperty getZip();
}
