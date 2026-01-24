package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.FileUtil;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Downloads a file from a URL link.
 * @author moist-mason
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

    public DownloadFile(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        FileUtil.download(logger, input, output);
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
