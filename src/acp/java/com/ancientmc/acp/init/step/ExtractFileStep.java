package com.ancientmc.acp.init.step;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;

/**
 * Extracts a single archive file.
 */
public class ExtractFileStep extends Step {
    /**
     * The input archive file getting extracted.
     */
    private File input;
    /**
     * The output directory that the archive contents are extracted into.
     */
    private File output;
    /**
     * The Gradle project.
     */
    private Project project;

    /**
     * Main extraction method. Uses Gradle's copy task and zip-tree function.
     * @param logger The gradle logger.
     * @param condition Boolean condition that determines if the step gets executed.
     */
    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);
        if (condition) {
            project.copy(action -> {
                action.from(project.zipTree(input));
                action.into(output);
            });
        }
    }

    public File getOutput() {
        return output;
    }

    public ExtractFileStep setInput(File input) {
        this.input = input;
        return this;
    }

    public ExtractFileStep setOutput(File output) {
        this.output = output;
        return this;
    }

    public ExtractFileStep setProject(Project project) {
        this.project = project;
        return this;
    }
}
