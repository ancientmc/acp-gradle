package com.ancientmc.acp.init.step;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ExtractNativesStep extends Step {
    private List<URL> urls;
    private File output;
    private Project project;

    @Override
    public void exec() {
        try {
            List<File> jars = new ArrayList<>();
            for(URL url : urls) {
                String path = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
                FileUtils.copyURLToFile(url, new File(output, path));
                jars.add(new File(output, path));
            }

            jars.forEach(jar -> {
                project.copy(action -> {
                   action.from(project.zipTree(jar));
                   action.into(project.file(output));
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getOutput() {
        return output;
    }

    public ExtractNativesStep setUrls(List<URL> urls) {
        this.urls = urls;
        return this;
    }

    public ExtractNativesStep setOutput(File output) {
        this.output = output;
        return this;
    }

    public ExtractNativesStep setProject(Project project) {
        this.project = project;
        return this;
    }
}
