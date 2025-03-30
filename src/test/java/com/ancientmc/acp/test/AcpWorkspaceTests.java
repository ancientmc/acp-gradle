package com.ancientmc.acp.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcpWorkspaceTests {
    /**
     * The JSON file containing our tests.
     */
    private static final File JSON_FILE = new File("tests.json");

    /**
     * The list of our tests.
     */
    private static final List<AcpTest> TESTS = new TestRegistry(JSON_FILE).get();

    /**
     * The root directory for our test workspace.
     */
    private static final File ROOT_TEST_DIR = new File("acp_test");
    private static final File TEST_DATA = new File("test_data");

    @Test
    public void testAll() {
        TESTS.forEach(test -> {
            setup(test);
            File testDir = new File(ROOT_TEST_DIR, test.version());
            doTest(test, testDir);
        });
    }

    public void doTest(AcpTest test, File testDir) {
        System.out.println("Test is: " + test.name());

        test.tasks().forEach(task -> {
            BuildResult result = GradleRunner.create()
                    .withProjectDir(testDir)
                    .withPluginClasspath()
                    .withArguments(task)
                    .build();
            assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));

            if (!task.contains("--")) { // filter out double-dash arg prefix (e.g. --stacktrace)
                List<TaskOutcome> outcomes = Arrays.asList(SUCCESS, UP_TO_DATE);
                assertTrue(outcomes.stream().anyMatch(outcome -> outcome == result.task(":" + task).getOutcome()));
                System.out.println("> Task " + task + " successful");
            }
        });
    }

    public void setup(AcpTest test) {
        try {
            if (!ROOT_TEST_DIR.exists()) {
                Files.createDirectories(ROOT_TEST_DIR.toPath());
            }

            File testDir = new File(ROOT_TEST_DIR, test.version());
            File mainData = new File(TEST_DATA, "main");
            Collection<File> mainFiles = FileUtils.listFiles(mainData, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);

            if (!testDir.exists()) {
                Files.createDirectories(testDir.toPath());
            }

            mainFiles.forEach(file -> copyFile(file, testDir, test));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyFile(File original, File testDir, AcpTest test) {
        try {
            File newFile = new File(testDir, original.getName());
            List<String> lines = Files.readAllLines(original.toPath());
            List<String> newLines = new ArrayList<>();

            lines.forEach(line -> {
                line = line.replace("{version}", test.version());
                line = line.replace("{loader}", test.loader());
                newLines.add(line);
            });

            write(newFile, newLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(File file, List<String> lines) {
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
