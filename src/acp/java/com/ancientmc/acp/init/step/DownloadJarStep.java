package com.ancientmc.acp.init.step;

import com.ancientmc.acp.utils.Json;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Extension of the DownloadFile step that allows for extra configuration for jar downloading.
 */
public class DownloadJarStep extends DownloadFileStep {
    /**
     * The Minecraft version, as determined in the ACP end-gradle workspace.
     */
    private String version;

    /**
     * This method parses through the JSON file to find the jar URL. The URL is retrieved via a method in the Json utilities class.
     * @param logger The gradle logger.
     * @param condition Boolean condition that determines if the step gets executed.
     * @see Json#getJarUrl(File, String)
     */
    @Override
    public void exec(Logger logger, boolean condition) {
        printMessage(logger, message, condition);
        if (condition) {
            try {
                File jar = new File(output, version + (input.getPath().contains("client") ? ".jar" : "-server.jar"));
                FileUtils.copyURLToFile(input, jar);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DownloadJarStep setVersion(String version) {
        this.version = version;
        return this;
    }

    public DownloadJarStep setInput(URL input) {
        super.setInput(input);
        return this;
    }

    public DownloadJarStep setOutput(File output) {
        super.setOutput(output);
        return this;
    }
}
