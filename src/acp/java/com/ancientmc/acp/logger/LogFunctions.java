package com.ancientmc.acp.logger;

import com.ancientmc.acp.util.FileUtil;

import java.io.File;
import java.net.URL;

/**
 * Contains methods for logger functions used several times.
 * @author moist-mason
 */
public class LogFunctions {

    /** ACP's logger. */
    private final AcpLogger logger;

    public LogFunctions(AcpLogger logger) {
        this.logger = logger;
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

    /**
     * Used to print the input and output of a copied path.
     * @param input The copy input path.
     * @param output The copy output path.
     */
    public void copy(File input, File output) {
        fileToFile(input, output);
        success(output.exists(), "Copied successfully");
    }

    /**
     * Used during file extraction.
     * @param input The input archive.
     * @param output The output directory.
     */
    public void extract(File input, File output) {
        fileToFile(input, output);
        success(!FileUtil.isDirectoryEmpty(output), "Extracted successfully");
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
        logger.toFile("Resolve -> {}", dependency);
    }

    /**
     * Used to log the input and outputs of an IO-based function.
     * @param input The input path.
     * @param output The output path.
     */
    private void io(String input, String output) {
        logger.toFile("Input -> {}", input);
        logger.toFile("Output -> {}", output);
    }

    /** Prints the message if the success condition is true. */
    private void success(boolean condition, String message) {
        if (condition) {
            logger.toFile(message);
        }
    }
}
