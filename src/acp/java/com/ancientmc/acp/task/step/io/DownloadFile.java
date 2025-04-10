package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.logger.QuickLog;
import com.ancientmc.acp.task.step.Step;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Downloads a file from a URL link.
 */
public class DownloadFile extends Step implements QuickLog {

    /**
     * The input URL.
     */
    protected URL input;

    /**
     * The downloaded file.
     */
    protected File output;

    public DownloadFile(Project project, AcpLogger logger, String message) {
        build(project, logger, message);
    }

    @Override
    public void action() {
        try {
            log();
            FileUtils.copyURLToFile(input, output);
        } catch (IOException e) {
            logger.error(project, e, "Download error.");
            throw new RuntimeException(e);
        }
    }


    @Override
    public void log() {
        logger.file(project, "Input -> {}", input.toString());
        logger.file(project, "Output -> {}", output.getPath());
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
