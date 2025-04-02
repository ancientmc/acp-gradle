package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.task.step.Step;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * Copies a file. Done using the Gradle API's CopySpec function.
 */
public class CopyFile extends Step {

    /**
     * The input file getting copied.
     */
    private File input;

    /**
     * The output directory that the input is copied into.
     */
    private File output;

    /**
     * Paths and files excluded from being copied.
     */
    private List<String> exclusions;

    public CopyFile(Project project, String phase, String message) {
        build(project, phase, message);
    }

    @Override
    public void action() {
        try {
            if (!output.exists()) {
                Files.createDirectories(output.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        project.copy(c -> {
            c.from(input);
            c.into(output);

            if (exclusions != null) {
                c.exclude(exclusions);
            }
        });
    }

    public CopyFile setInput(File input) {
        this.input = input;
        return this;
    }

    public CopyFile setOutput(File output) {
        this.output = output;
        return this;
    }

    public CopyFile setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
        return this;
    }

    public CopyFile setExclusions(String exclusions) {
        return setExclusions(Collections.singletonList(exclusions));
    }
}
