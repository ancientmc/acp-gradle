package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.task.step.Step;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

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

    /**
     * This method uses a function from Apache Commons-IO to download a file from a URL.
     * @param logger The gradle logger.
     * @param condition Boolean condition that determines if the step gets executed.
     */
    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        if (condition) {
            try {
                FileUtils.copyURLToFile(input, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public File getOutput() {
        return output;
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
