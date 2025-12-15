package com.ancientmc.acp.test;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcpTest {

    /**
     * The registry for all our tests.
     */
    public static final TestRegistry REGISTRY = new TestRegistry(new File("test_data/tests.json"));

    // All versions
    @Test public void testAll() { REGISTRY.tests.forEach(o -> doTest(o.name())); }

    // Individual versions
    @Test public void testClassicOne() { doTest("classic_one"); }
    @Test public void testClassicTwo() { doTest("classic_two"); }
    @Test public void testAlphaLegacyVanilla() { doTest("legacyAlpha_vanilla"); }
    @Test public void testAlphaLegacyModded() { doTest("legacyAlpha_modded"); }

    // Non-version-specific tasks
    @Test public void testSupportVersionsTask() { doTest("supported_versions"); }
    @Test public void testCleanupTask() { doTest("cleanup"); }

    public void doTest(String name) {
        TestObject test = getTest(name);
        System.out.println("Test is: " + test.name());
        TestSetup.start(test);
        execute(test);
    }

    public static void execute(TestObject test) {
        File testDir = new File(TestPaths.ROOT_TEST_DIR, test.name());

        test.tasks().forEach(task -> {
            System.out.println("> Running task " + task);

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

            System.out.println("> OUTPUT:\n" + result.getOutput());
        });
    }

    public static TestObject getTest(String name) {
        return REGISTRY.getTest(name);
    }
}
