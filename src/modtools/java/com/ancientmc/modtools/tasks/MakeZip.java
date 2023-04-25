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
        File mappedDirectory = getClassDirectory().get().getAsFile();
        File originalHash = getOriginalHash().get().getAsFile();
        File moddedHash = getModdedHash().get().getAsFile();
        File srg = getSrg().get().getAsFile();
        File obfDirectory = getObfuscatedClassDirectory().get().getAsFile();
        File zip = getZip().get().getAsFile();

        try {
            // Retrieve hash maps.
            Map<String, String> originalMap = getHashMap(originalHash);
            Map<String, String> moddedMap = getHashMap(moddedHash);

            // Remove ACP start class from map.
            moddedMap.remove("acp\\client\\Start");

            List<File> moddedClasses = new ArrayList<>();
            moddedMap.forEach((name, hash) -> {
                if (!originalMap.containsValue(hash)) {
                    File moddedClass = new File(mappedDirectory, name + ".class");
                    moddedClasses.add(moddedClass);
                }
            });

            make(zip, moddedClasses, obfDirectory, mappedDirectory, srg);
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
     * Creates the Zip file.
     */
    public void make(File zip, List<File> classes, File obfDirectory, File mappedDirectory, File srg) throws IOException {
        List<File> files = new ArrayList<>();
        Map<String, String> classMap = Utils.getClassMap(srg);
        classes.forEach(cls -> {
            // Shrink the class name to its bare necessities, i.e. without full buildpath and .class suffix, and replace file separator with '/' character.
            String shrunkName = mappedDirectory.toPath().relativize(cls.toPath()).toString().replace(".class", "").replace(File.separator, "/");

            // Try to retrieve obfuscated class name only if the shrunk name is present in the class map. Accounts for mod classes which don't have values in the class map.
            String className = classMap.containsValue(shrunkName) ? getObfName(shrunkName, classMap) : shrunkName;

            // Retrieve class file currently being parsed from compiled re-obfuscated class directory.
            String clazz = obfDirectory.getAbsolutePath() + "/" + className + ".class";
            files.add(new File(clazz));
        });
        Utils.compress(files, zip);
    }

    /**
     * Get obfuscated class name.
     */
    public String getObfName(String name, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (name.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @InputDirectory
    public abstract DirectoryProperty getClassDirectory();

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
