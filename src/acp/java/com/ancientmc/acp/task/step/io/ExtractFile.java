package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.task.step.Step;
import org.gradle.api.Project;

import java.io.File;
import java.util.List;

/**
 * Extracts a single archive file.
 */
public class ExtractFile extends Step {

    /**
     * The input archive file getting extracted.
     */
    private File input;

    /**
     * The output directory that the archive contents are extracted into.
     */
    private File output;

    /**
     * Paths and files excluded from extraction.
     */
    protected List<String> exclusions;

    protected List<String> inclusions;

    public ExtractFile(Project project, String phase, String message) {
        build(project, phase, message);
    }

    /**
     * Main extraction method. Uses Gradle's copy task and zip-tree function.
     */
    @Override
    public void action() {
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
}
