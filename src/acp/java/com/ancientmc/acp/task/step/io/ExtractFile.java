package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.logger.QuickLog;
import com.ancientmc.acp.task.step.Step;
import org.gradle.api.Project;

import java.io.File;
import java.util.List;

/**
 * Extracts a single archive file.
 * @author moist-mason
 */
public class ExtractFile extends Step implements QuickLog {

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

    public ExtractFile(Project project, AcpLogger logger, String message) {
        build(project, logger, message);
    }

    /**
     * Main extraction method. Uses Gradle's copy task and zip-tree function.
     */
    @Override
    public void action() {
        log();
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


    @Override
    public void log() {
        logger.file(project, "Input archive -> {}", input.getAbsolutePath());
        logger.file(project, "Output directory -> {}", output.getAbsolutePath());
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
