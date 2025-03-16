package com.ancientmc.acp.tasks.step;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * Copies a file. Done using the Gradle API's CopySpec function.
 */
public class CopyFileStep extends Step {

    /**
     * The input file getting copied.
     */
    protected File input;

    /**
     * The output directory that the input is copied into.
     */
    protected File output;

    /**
     * Paths and files excluded from being copied.
     */
    protected List<String> exclusions;

    /**
     * The Gradle project.
     */
    private Project project;

    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        try {
            if (!output.getParentFile().exists()) {
                Files.createDirectories(output.getParentFile().toPath());
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

    public File getOutput() {
        return output;
    }

    public CopyFileStep setInput(File input) {
        this.input = input;
        return this;
    }

    public CopyFileStep setOutput(File output) {
        this.output = output;
        return this;
    }

    public CopyFileStep setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
        return this;
    }

    public CopyFileStep setExclusions(String exclusions) {
        return setExclusions(Collections.singletonList(exclusions));
    }

    public CopyFileStep setProject(Project project) {
        this.project = project;
        return this;
    }
}
