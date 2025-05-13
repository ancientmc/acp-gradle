package com.ancientmc.acp.task.step.io;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.Json;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts native libraries required for Minecraft to run into the designated folder.
 */
public class ExtractNatives extends Step {

    /**
     * The list of URLs for native libraries from Minecraft's website. The URLs are retrieved via a method in utils.Json
     * @see Json#getNativeUrls(File)
     */
    private List<URL> urls;

    /**
     * The output directory that will contain the native files.
     */
    private File output;

    public ExtractNatives(Project project, AcpLogger logger, String message) {
        build(project, logger, message);
    }

    /**
     * To extract the natives, we first download the JAR files in the URLS. Then for each file, we extract the native libraries
     * from their JAR files into the output folder.
     */
    @Override
    public void action() throws IOException {
        List<File> jars = new ArrayList<>();

        for (URL url : urls) {
            String path = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
            FileUtils.copyURLToFile(url, new File(output, path));
            jars.add(new File(output, path));
        }

        jars.forEach(jar -> project.copy(action -> {
            logger.functions().fileToFile(jar, output);
            action.from(project.zipTree(jar));
            action.into(project.file(output));
        }));
    }

    public ExtractNatives setUrls(List<URL> urls) {
        this.urls = urls;
        return this;
    }

    public ExtractNatives setOutput(File output) {
        this.output = output;
        return this;
    }
}
