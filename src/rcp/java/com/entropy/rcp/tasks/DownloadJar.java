package com.entropy.rcp.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract class DownloadJar extends DefaultTask {

    @TaskAction
    public void downloadJar() throws IOException {
        URL url = getURL().get();
        File output = getOutput().get().getAsFile();

        FileUtils.copyURLToFile(url, output);
    }

    @Input
    public abstract Property<URL> getURL();

    @OutputFile
    public abstract RegularFileProperty getOutput();
}
