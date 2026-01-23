package com.ancientmc.acp.logger;

import com.ancientmc.acp.util.FileUtil;
import org.gradle.api.Project;

import java.io.File;
import java.net.URL;

/**
 * Contains methods for logger functions used several times.
 * @author moist-mason
 */
public class LogFunctions {

    /**
     * ACP's logger.
     */
    private final AcpLogger logger;

    /**
     * The gradle project.
     */
    private final Project project;

    public LogFunctions(AcpLogger logger, Project project) {
        this.logger = logger;
        this.project = project;
    }

    /**
     * Used to print the input and output of a downloaded URL.
     * @param input The URL.
     * @param output The downloaded file.
     */
    public void download(URL input, File output) {
        io(input.toString(), output.getAbsolutePath());
        success(output.exists(), "Downloaded successfully");
    }

    public void copy(File input, File output) {
        fileToFile(input, output);
        success(output.exists(), "Copied successfully");
    }

    public void extract(File input, File output) {
        fileToFile(input, output);
        success(!FileUtil.directoryCondition(output), "Extracted successfully");
    }

    /**
     * Used to print the input and output of a moved or copied file(s).
     * @param input The input file or directory.
     * @param output The output file or directory.
     */
    public void fileToFile(File input, File output) {
        io(input.getAbsolutePath(), output.getAbsolutePath());
    }


    /**
     * Used during library resolution.
     * @param dependency The name of a dependency being resolved.
     */
    public void resolve(String dependency) {
        logger.file(project, "Resolve -> {}", dependency);
    }

    /**
     * Used to log the input and outputs of an IO-based function.
     */
    private void io(String input, String output) {
        logger.file(project, "Input -> {}", input);
        logger.file(project, "Output -> {}", output);
    }


    /**
     * Prints the message if the success condition is true.
     */
    private void success(boolean condition, String message) {
        if (condition) {
            logger.file(project, message);
        }
    }
}
