package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.FileUtil;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * Copies a file. Done using the Gradle API's CopySpec function.
 * @author moist-mason
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

    public CopyFile(Project project, AcpLogger logger, String message) {
        build(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        FileUtil.createDirectory(output);

        logger.functions().fileToFile(input, output);
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
