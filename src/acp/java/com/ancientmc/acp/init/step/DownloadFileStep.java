package com.ancientmc.acp.init.step;

import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Downloads a file from a URL link.
 */
public class DownloadFileStep extends Step {
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
                e.printStackTrace();
            }
        }
    }

    public File getOutput() {
        return output;
    }

    public DownloadFileStep setInput(URL input) {
        this.input = input;
        return this;
    }

    public DownloadFileStep setOutput(File output) {
        this.output = output;
        return this;
    }
}
