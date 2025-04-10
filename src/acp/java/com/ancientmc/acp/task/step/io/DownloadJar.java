package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.Json;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Extension of the DownloadFile step that allows for extra configuration for jar downloading.
 */
public class DownloadJar extends DownloadFile {

    public DownloadJar(Project project, AcpLogger logger, String message) {
        super(project, logger, message);
    }

    /**
     * This method parses through the JSON file to find the jar URL. The URL is retrieved via a method in the Json utilities class.
     * @see Json#getJarUrl(File, String)
     */
    public void action() {
        try {
            File jar = new File(output, input.getPath().contains("client") ? "client.jar" : "server.jar");
            log(jar);
            FileUtils.copyURLToFile(input, jar);
        } catch (IOException e) {
            logger.error(project, e, "Download error.");
            throw new RuntimeException(e);
        }
    }


    /**
     * Based on the parent class's log action to account for the JAR's path.
     * @param jar The JAR path.
     */
    public void log(File jar) {
        logger.file(project, "Input -> {}", input.getPath());
        logger.file(project, "Output -> {}", jar.getAbsolutePath());
    }


    public DownloadJar setInput(URL input) {
        super.setInput(input);
        return this;
    }

    public DownloadJar setOutput(File output) {
        super.setOutput(output);
        return this;
    }
}
