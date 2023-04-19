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
            name = name.replace(".class", "");
            name = name.replace(directory.getAbsolutePath() + File.separator, "");
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

    @InputDirectory
    public abstract RegularFileProperty getClassesDirectory();

    @OutputFile
    public abstract RegularFileProperty getOutput();

}
