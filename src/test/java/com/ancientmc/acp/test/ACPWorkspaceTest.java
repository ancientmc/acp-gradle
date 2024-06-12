package com.ancientmc.acp.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ACPWorkspaceTest {
    /**
     * The generated directory for our test workspace.
     */
    private File testDir;
    /**
     * The "test_data" folder, which contains template files used to make an ACP test workspace.
     */
    private File testData;

    /**
     * Sets up the test workspace.
     * @throws IOException
     */
    @BeforeEach
    public void setup() throws IOException {
        testDir = new File("acp_test\\");
        testData = new File("test_data\\main\\");

        // Each template, including the build file, gets copied over to the test workspace directory.
        Collection<File> files = FileUtils.listFiles(testData, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        for (File file : files) {
            String newPath = testData.toPath().relativize(file.toPath()).toString();
            FileUtils.copyFile(file, new File(testDir, newPath));
        }

        // Generate the src/main/resources path. This prevents an File not found error in the test build.gradle.
        FileUtils.forceMkdir(new File(testDir, "src\\main\\resources"));
    }

    /** Cleans the workspace. **/
    @Test public void testClean() { doTest("clean"); }

    /** Sets up a vanilla ACP workspace. The "modpatches" folder must be deleted before this can be run.**/
    @Test public void testSetup() { doTest("clean", "setup"); }

    /** Sets up an ACP workspace with the modloader injected, but with no additional mods installed. **/
    @Test public void testSetupModdedClean() { doTest("clean", "downloadModLoader", "setup"); }

    /** Sets up an ACP workspace with the modloader injected, as well as the test ruby mod copied to the source path. **/
    @Test public void testSetupModdedRuby() { doTestModded("clean", "downloadModLoader", "setup"); }

    /** Runs the game. **/
    @Test public void testRunClient() { doTest("runClient"); }

    /** Generates DiffPatches. **/
    @Test public void testGeneratePatches() { doTestModded("makeDiffPatches"); }

    /** Makes the ZIP containing modified class files. **/
    @Test public void testMakeZip() { doTestModded ("makeZip"); }

    /**
     * Runs a test.
     * @param args Gradle args, including tasks being run.
     */
    public void doTest(String... args) {
        for (String arg : args) {
            BuildResult result = GradleRunner.create()
                    .withProjectDir(testDir)
                    .withPluginClasspath()
                    .withArguments(arg)
                    .build();
            assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));

            if (!arg.contains("--")) { // filter out double-dash arg prefix (e.g. --stacktrace)
                List<TaskOutcome> outcomes = Arrays.asList(SUCCESS, UP_TO_DATE);
                assertTrue(outcomes.stream().anyMatch(outcome -> outcome == result.task(":" + arg).getOutcome()));
            }
        }
    }

    /**
     * Same as above, but injects a test ruby mod into the source path after the test.
     * @param args Gradle args, including tasks being run.
     */
    public void doTestModded(String... args) {
        try {
            doTest(args);
            injectRubyTestMod();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies the ruby test mod into the source path.
     * @throws IOException
     */
    public void injectRubyTestMod() throws IOException {
        File ruby = new File("test_data\\ruby\\");
        Collection<File> files = FileUtils.listFiles(ruby, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);

        for (File file : files) {
            String newPath = ruby.toPath().relativize(file.toPath()).toString();
            File newFile = new File(testDir, newPath);

            if (!newFile.exists()) {
                FileUtils.copyFile(file, newFile);
            }
        }
    }
}
