package com.ancientmc.acp.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

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
public abstract class GenerateHashes extends DefaultTask {
    @TaskAction
    public void exec() {
        File directory = getClassesDirectory().get().getAsFile();
        File output = getOutput().get().getAsFile();
        try {
            run(directory, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void run(File directory, File out) throws IOException {
        Map<String, String> map = new HashMap<>();
        Collection<File> classes = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        classes.forEach(cls -> {
            String hash = getHash(cls);
            String name = cls.getAbsolutePath();
            name = name.replace(".class", "")
                    .replace(directory.getAbsolutePath() + File.separator, "")
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
     * Calculates an MD5 hash from the given class.
     * @param cls The class file.
     * @return The hash.
     */
    public static String getHash(File cls) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(Files.readAllBytes(cls.toPath()));

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

    /**
     * The output text file containing the hash values.
     */
    @OutputFile
    public abstract RegularFileProperty getOutput();

}
