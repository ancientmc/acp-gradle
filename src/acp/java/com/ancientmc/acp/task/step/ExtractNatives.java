package com.ancientmc.acp.task.step;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.FileUtil;
import com.ancientmc.acp.util.Json;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts native libraries required for Minecraft to run into the designated folder.
 * @author moist-mason
 */
public class ExtractNatives extends Step {

    /**
     * The list of URLs for native libraries from Minecraft's website. The URLs are retrieved via a method in util.Json
     * @see Json#getNativeUrls(File)
     */
    private List<URL> urls;

    /**
     * The output directory that will contain the native files.
     */
    private File output;

    public ExtractNatives(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    /**
     * To extract the natives, we first download the JAR files in the URLS. Then for each file, we extract the native libraries
     * from their JAR files into the output folder.
     */
    @Override
    public void action() throws IOException {
        List<File> jars = new ArrayList<>();
        FileUtil.createDirectory(output);

        for (URL url : urls) {
            String path = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
            FileUtil.download(logger, url, new File(output, path));
            jars.add(new File(output, path));
        }

        for (File jar : jars) {
            FileUtil.extract(project, logger, jar, output);
        }
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
