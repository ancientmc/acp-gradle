package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.task.step.Step;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Downloads a file from a URL link.
 */
public class DownloadFile extends Step {

    /**
     * The input URL.
     */
    protected URL input;

    /**
     * The downloaded file.
     */
    protected File output;

    public DownloadFile(Project project, String phase, String message) {
        build(project, phase, message);
    }

    @Override
    public void action() {
        try {
            FileUtils.copyURLToFile(input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DownloadFile setInput(URL input) {
        this.input = input;
        return this;
    }

    public DownloadFile setOutput(File output) {
        this.output = output;
        return this;
    }
}
