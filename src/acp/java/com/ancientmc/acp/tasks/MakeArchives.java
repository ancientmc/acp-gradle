package com.ancientmc.acp.tasks;

import com.ancientmc.acp.util.Util;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Makes two archives (ZIP and TAR) of mod classes and/or modified Minecraft classes for distribution.
 */
public abstract class MakeArchives extends DefaultTask {

    @TaskAction
    public void exec() {
        File hashDirectory = getHashDirectory().getAsFile().get();
        File srg = getSrg().getAsFile().get();
        File obfDirectory = getObfuscatedClassDirectory().getAsFile().get();
        File resourcesDirectory = getResourcesDirectory().getAsFile().get();
        File archiveDirectory = getArchiveDirectory().getAsFile().get();

        try {
            // Retrieve hash maps.
            Map<String, String> vanillaMap = getHashMap(new File(hashDirectory, "vanilla.md5"));
            Map<String, String> moddedMap = getHashMap(new File(hashDirectory, "modded.md5"));
            Map<String, String> classMap = Util.getClassMap(srg);

            // Remove ACP start class from map.
            moddedMap.remove("acp/client/Start");
            List<File> moddedFiles = new ArrayList<>();

            // Key -> the resource. Value -> the resource's parent directories.
            Map<File, String> moddedResources = new HashMap<>();

            moddedMap.forEach((name, hash) -> {
                if (!vanillaMap.containsValue(hash)) {
                    if (name.startsWith("net/minecraft/src/") || (name.startsWith("com/mojang"))) {
                        // Add classes. Get the file names without packages
                        String strippedName = name.substring(name.lastIndexOf('/') + 1);

                        String className = classMap.containsValue(name) ? getObfName(name, classMap) : strippedName;
                        File moddedClass = getProject().file(obfDirectory.getPath() + "/" + className + ".class");
                        moddedFiles.add(moddedClass);
                    } else {
                        // Add resources
                        File moddedResource = getProject().file(resourcesDirectory.getPath() + "/" + name);
                        String parent = name.substring(0, name.lastIndexOf('/'));
                        moddedResources.put(moddedResource, parent);
                    }
                }
            });

            // mod version
            String version = this.getProject().getVersion().toString();

            Util.compress(moddedFiles, moddedResources, archiveDirectory, version);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                .map(Map.Entry::getKey).findAny().orElse(null);
    }

    @InputDirectory
    public abstract DirectoryProperty getObfuscatedClassDirectory();

    @InputDirectory
    public abstract DirectoryProperty getResourcesDirectory();

    @InputDirectory
    public abstract DirectoryProperty getHashDirectory();

    @InputFile
    public abstract RegularFileProperty getSrg();

    @OutputDirectory
    public abstract DirectoryProperty getArchiveDirectory();
}
