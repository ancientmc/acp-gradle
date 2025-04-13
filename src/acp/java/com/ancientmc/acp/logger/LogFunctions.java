package com.ancientmc.acp.logger;

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
    public void urlToFile(URL input, File output) {
        io(input.toString(), output.getAbsolutePath());
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
    private void io(String in, String out) {
        logger.file(project, "Input -> {}", in);
        logger.file(project, "Output -> {}", out);
    }
}
