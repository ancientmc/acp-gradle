package com.ancientmc.acp.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a text file containing md5 hashes corresponding to the compiled Minecraft classes.
 * The text file is formatted as such:
 *      pkg\class1 hash1
 *      pkg\class2 hash2
 *      etc...
 * These hash values are used to determine whether a compiled class file has been modified.
 */
public abstract class MakeHashes extends DefaultTask {
    @TaskAction
    public void exec() {
        try {
            File classDirectory = getClassesDirectory().get().getAsFile();
            File resourceDirectory = getResourcesDirectory().get().getAsFile();
            File output = getOutput().get().getAsFile();
            run(classDirectory, resourceDirectory, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Main execution method for the text file generation. A map gets generated and is then written out as a text file.
     * @param classDirectory The directory containing the class files.
     * @param resourceDirectory The directory containing the resource files.
     * @param out The output text file containing the hash values.
     * @throws IOException exception.
     */
    public static void run(File classDirectory, File resourceDirectory, File out) throws IOException {
        Map<String, String> map = new HashMap<>();
        Collection<File> classes = FileUtils.listFiles(classDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        Collection<File> resources = FileUtils.listFiles(resourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);

        classes.forEach(cls -> {
            String hash = getHash(cls);
            String name = cls.getAbsolutePath();
            name = name.replace(".class", "")
                    .replace(classDirectory.getAbsolutePath() + File.separator, "")
                    .replace(File.separator, "/");
            map.put(name, hash);
        });

        resources.forEach(rs -> {
            String hash = getHash(rs);
            String name = rs.getAbsolutePath();
            name = name.replace(resourceDirectory.getAbsolutePath() + File.separator, "")
                    .replace(File.separator, "/");
            map.put(name, hash);
        });

        try (FileWriter writer = new FileWriter(out)) {
            map.forEach((name, hash) -> {
                try {
                    writer.write(name + " " + hash + "\n");
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Calculates an MD5 hash from the given file.
     * @param file The file.
     * @return The hash.
     */
    public static String getHash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(Files.readAllBytes(file.toPath()));

            return new BigInteger(1, bytes).toString(16);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The directory containing the class files.
     */
    @InputDirectory
    public abstract RegularFileProperty getClassesDirectory();

    @InputDirectory
    public abstract RegularFileProperty getResourcesDirectory();

    /**
     * The output text file containing the hash values.
     */
    @OutputFile
    public abstract RegularFileProperty getOutput();
}
