package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.task.step.Step;
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
public class CopyFile extends Step {

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

        if (condition) {
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
    }

    public File getOutput() {
        return output;
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

    public CopyFile setProject(Project project) {
        this.project = project;
        return this;
    }
}
