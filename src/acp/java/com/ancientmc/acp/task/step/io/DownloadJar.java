package com.ancientmc.acp.task.step.io;

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

    public DownloadJar(Project project, String phase, String message) {
        super(project, phase, message);
    }

    /**
     * This method parses through the JSON file to find the jar URL. The URL is retrieved via a method in the Json utilities class.
     * @see Json#getJarUrl(File, String)
     */
    public void action() {
        try {
            File jar = new File(output, input.getPath().contains("client") ? "client.jar" : "server.jar");
            FileUtils.copyURLToFile(input, jar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
