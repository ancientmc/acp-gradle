package com.ancientmc.acp.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestSetup {

    /**
     * Starts the setup for this test.
     * @param test The test object.
     */
    public static void start(TestObject test) {
        try {
            if (!TestPaths.ROOT_TEST_DIR.exists()) {
                Files.createDirectories(TestPaths.ROOT_TEST_DIR.toPath());
            }

            File testDir = new File(TestPaths.ROOT_TEST_DIR, test.name());
            File mainData = new File(TestPaths.TEST_DATA, "main");
            Collection<File> mainFiles = FileUtils.listFiles(mainData, TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);

            if (!testDir.exists()) {
                Files.createDirectories(testDir.toPath());
            }

            for (File original : mainFiles) {
                String path = mainData.toPath().relativize(original.toPath()).toString();
                File copied = new File(testDir, path);

                if (!copied.getParentFile().exists()) {
                    Files.createDirectories(copied.getParentFile().toPath());
                }

                copyFile(original, copied, test);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyFile(File original, File copied, TestObject test) {
        try {
            List<String> lines = Files.readAllLines(original.toPath());
            List<String> newLines = new ArrayList<>();

            lines.forEach(line -> {
                line = line.replace("{version}", test.version());
                line = line.replace("{loader}", test.loader());
                line = line.replace("{name}", test.name());
                newLines.add(line);
            });

            write(copied, newLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(File file, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
