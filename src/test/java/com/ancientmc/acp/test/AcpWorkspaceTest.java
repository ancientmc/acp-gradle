package com.ancientmc.acp.test;

import com.ancientmc.acp.utils.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcpWorkspaceTest {
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

        // Each template, including the build.gradle, gets copied over to the test workspace directory.
        Collection<File> files = FileUtils.listFiles(testData, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        for (File file : files) {
            String newPath = testData.toPath().relativize(file.toPath()).toString();
            FileUtils.copyFile(file, new File(testDir, newPath));
        }

        // Generate the src/main/resources path.
        FileUtils.forceMkdir(new File(testDir, "src\\main\\resources"));
    }

    /**
     * Runs the "setup" task in the ACP workspace, which decompiles the game.
     */
    @Test
    public void testSetup() {
        BuildResult result = GradleRunner.create()
                .withProjectDir(testDir)
                .withPluginClasspath()
                .withArguments("setup")
                .build();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
        assertEquals(SUCCESS, result.task(":setup").getOutcome());
    }
}
