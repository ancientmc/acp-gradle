package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.Util;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeArchives extends Step {

    /**
     * The Gradle project.
     */
    protected Project project;

    /**
     * The TSRG file.
     */
    protected File srg;

    /**
     * The directory containing our obfuscated class files (build/modding/reobfClasses/)
     */
    protected File obfDirectory;

    /**
     * The directory containing our resources (src/main/resources/).
     */
    protected File resourceDirectory;

    /**
     * The directory containing our hash files.
     */
    protected File hashDirectory;

    /**
     * The directory where the ZIP and TAR archives get put into.
     */
    protected File archiveDirectory;

    public void exec(Logger logger, boolean condition) {
        try {

            if (!archiveDirectory.exists()) {
                Files.createDirectories(archiveDirectory.toPath());
            }

            Map<String, String> vanillaMap = getHashMap(new File(hashDirectory, "vanilla.md5"));
            Map<String, String> moddedMap = getHashMap(new File(hashDirectory, "modded.md5"));
            Map<String, String> classMap = Util.getClassMap(srg);

            // Remove ACP start classes from map.
            List<Map.Entry<String, String>> entries = moddedMap.entrySet().stream()
                    .filter(e -> e.getKey().startsWith("acp/client/")).toList();
            entries.forEach(moddedMap.entrySet()::remove);

            // key -> the file. value -> the file path.
            Map<File, String> moddedFiles = new HashMap<>();

            moddedMap.forEach((name, hash) -> {
                if (!vanillaMap.containsValue(hash)) {
                    if (name.startsWith("net/minecraft/src/") || (name.startsWith("com/mojang"))) {

                        String className = classMap.containsValue(name) ? getObfName(name, classMap) : name;
                        File moddedClass = project.file(obfDirectory.getPath() + "/" + className + ".class");
                        moddedFiles.put(moddedClass, name.substring(0, name.lastIndexOf('/')));

                    } else {

                        // Add resources
                        File moddedResource = project.file(resourceDirectory.getPath() + "/" + name);
                        moddedFiles.put(moddedResource, name.substring(0, name.lastIndexOf('/')));
                    }
                }
            });

            // Mod version -> version in build.gradle or somewhere else defined by the end-user.
            String version = project.getVersion().toString();

            Util.compress(moddedFiles, archiveDirectory, version);
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
     * Gets obfuscated class name.
     */
    public String getObfName(String name, Map<String, String> map) {
        return map.entrySet().stream()
                .filter(entry -> name.equals(entry.getValue()))
                .map(Map.Entry::getKey).findAny().orElse(null);
    }

    public MakeArchives setProject(Project project) {
        this.project = project;
        return this;
    }

    public MakeArchives setSrg(File srg) {
        this.srg = srg;
        return this;
    }

    public MakeArchives setObfDirectory(File obfDirectory) {
        this.obfDirectory = obfDirectory;
        return this;
    }

    public MakeArchives setResourceDirectory(File resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
        return this;
    }

    public MakeArchives setHashDirectory(File hashDirectory) {
        this.hashDirectory = hashDirectory;
        return this;
    }

    public MakeArchives setArchiveDirectory(File archiveDirectory) {
        this.archiveDirectory = archiveDirectory;
        return this;
    }
}
