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

public class AcpWorkspaceTest {

    /**
     * The generated directory for our test workspace.
     */
    private File testDir;

    /**
     * Sets up the test workspace.
     * @throws IOException exception.
     */
    @BeforeEach
    public void setup() throws IOException {
        testDir = new File("acp_test/");

        //  The "test_data" folder, which contains template files used to make an ACP test workspace.
        File testData = new File("test_data/main/");

        // Each template, including the build file, gets copied over to the test workspace directory.
        Collection<File> files = FileUtils.listFiles(testData, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        for (File file : files) {
            String newPath = testData.toPath().relativize(file.toPath()).toString();
            FileUtils.copyFile(file, new File(testDir, newPath));
        }

        // Generate the src/main/resources path. This prevents a File not found error in the test build.gradle.
        FileUtils.forceMkdir(new File(testDir, "src/main/resources"));
    }

    /** Runs all tests. The test will pause when Minecraft is running and only continue until after it's closed.
     * We only need to worry about the "runRuby" test, to ensure our ruby mod's additions are getting registered properly.
     * Otherwise, just close the game after it launches.
     **/
    @Test public void testDecompileAll() {
        testDecompile();
    }

    @Test public void testInitialize() { doTest("initialize"); }

    /** Sets up a vanilla ACP workspace. The "modpatches" folder must be deleted before this can be run.**/
    @Test public void testDecompile() { doTest("decompile"); }

    /** Runs the game. **/
    @Test public void testRunClient() { doTest("runClient"); }

    @Test public void testModLoader() { doTest("downloadModLoader"); }

    /** Builds our mod. **/
    @Test public void testBuild() { doTest("buildMod"); }

    /**
     * THE FOLLOWING BELOW IS THE "NEEDS TO BE REWORKED" ZONE!
     */

    /** Sets up an ACP workspace with the modloader injected, but with no additional mods installed. **/
    @Test public void testDecompileModdedClean() { doTest("decompileModded", "clean", "downloadModLoader", "decompile", "runClient"); }

    /** Sets up an ACP workspace with the modloader injected, as well as the test ruby mod copied to the source path.
     * Runs the game and also exports patches and archives. **/
    @Test public void testDecompileRuby() {
        doTestRuby("decompileRuby", "downloadModLoader", "decompile");
        doTest("runRuby", "runClient");
        doTest("exportRuby", "buildMod");
    }

    /**
     * Runs a single-argument test who shares a name with its argument.
     * @param name The name of the test.
     */
    public void doTest(String name) {
        doTest(name, name);
    }

    /**
     * Runs a test.
     * @param args Gradle args, including tasks being run.
     */
    public void doTest(String name, String... args) {
        System.out.println("Running test " + name);

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
                System.out.println("> Task " + arg + " successful");
            }
        }
        System.out.println("Test " + name + " successful");
    }

    /**
     * Same as above, but injects a test ruby mod into the source path after the test.
     * @param args Gradle args, including tasks being run.
     */
    public void doTestRuby(String name, String... args) {
        try {
            doTest(name, args);
            injectRubyTestMod();
            System.out.println("Ruby mod injection successful");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copies the ruby test mod into the source path.
     * @throws IOException exception.
     */
    public void injectRubyTestMod() throws IOException {
        File ruby = new File("test_data/ruby/");
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
