package com.ancientmc.acp.utils;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.nio.file.Paths.get;

public class GenerateHashesFunction {
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
}
