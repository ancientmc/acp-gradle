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
    public static void start(TestObject test, boolean ruby) {
        try {
            createDirectory(TestPaths.ROOT_TEST_DIR);
            File testDir = new File(TestPaths.ROOT_TEST_DIR, test.name());
            createDirectory(testDir);
            copyFileTree(testDir, test, ruby);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets list of files to copy
     * @param ruby If the ruby mod is being injected.
     * @return The files.
     */
    private static Collection<File> getFiles(boolean ruby) {
        Collection<File> files = FileUtils.listFiles(TestPaths.TEST_DATA, TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);

        if (!ruby) {
            files.removeAll(files.stream().filter(x -> x.getAbsolutePath().contains("ruby")).toList());
        }

        return files;
    }

    private static void copyFileTree(File testDir, TestObject test, boolean ruby) throws IOException {
        Collection<File> files = getFiles(ruby);

        for (File original : files) {
            String path = TestPaths.TEST_DATA.toPath().relativize(original.toPath()).toString();
            File copied = new File(testDir, path);
            System.out.println(copied.getAbsolutePath());

            if (!copied.getParentFile().exists()) {
                Files.createDirectories(copied.getParentFile().toPath());
            }

            if (!original.getName().endsWith(".png")) {
                copyTextFile(original, copied, test);
            } else {
                FileUtils.copyFile(original, copied);
            }
        }
    }

    private static void copyTextFile(File original, File copied, TestObject test) {
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

    private static void createDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            Files.createDirectories(directory.toPath());
        }
    }
}
