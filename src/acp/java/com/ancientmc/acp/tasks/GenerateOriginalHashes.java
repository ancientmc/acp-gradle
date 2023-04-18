package com.ancientmc.acp.tasks;

import com.ancientmc.acp.utils.GenerateHashesFunction;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;

public abstract class GenerateOriginalHashes extends DefaultTask {
    @TaskAction
    public void exec() {
        File directory = getClassesDirectory().get().getAsFile();
        File output = getOutput().get().getAsFile();
        try {
            GenerateHashesFunction.run(directory, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @InputDirectory
    public abstract RegularFileProperty getClassesDirectory();

    @OutputFile
    public abstract RegularFileProperty getOutput();

}
