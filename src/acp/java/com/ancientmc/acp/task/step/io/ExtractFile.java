package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.task.step.Step;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.List;

/**
 * Extracts a single archive file.
 */
public class ExtractFile extends Step {

    /**
     * The input archive file getting extracted.
     */
    protected File input;

    /**
     * The output directory that the archive contents are extracted into.
     */
    protected File output;

    /**
     * The Gradle project.
     */
    protected Project project;

    /**
     * Paths and files excluded from extraction.
     */
    protected List<String> exclusions;

    protected List<String> inclusions;

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

                if (inclusions != null) {
                    action.include(inclusions);
                }

                if (exclusions != null) {
                    action.exclude(exclusions);
                }
            });
        }
    }

    public File getOutput() {
        return output;
    }

    public ExtractFile setInput(File input) {
        this.input = input;
        return this;
    }

    public ExtractFile setOutput(File output) {
        this.output = output;
        return this;
    }

    public ExtractFile setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
        return this;
    }

    public ExtractFile setInclusions(List<String> inclusions) {
        this.inclusions = inclusions;
        return this;
    }

    public ExtractFile setProject(Project project) {
        this.project = project;
        return this;
    }
}
