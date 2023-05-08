package com.ancientmc.acp.init.step;

import org.gradle.api.Project;

import java.io.File;

public class ExtractFileStep extends Step {
    private File input;
    private File output;
    private Project project;

    @Override
    public void exec() {
        project.copy(action -> {
           action.from(project.zipTree(input));
           action.into(output);
        });
    }

    public File getInput() {
        return input;
    }

    public File getOutput() {
        return output;
    }

    public ExtractFileStep setInput(File input) {
        this.input = input;
        return this;
    }

    public ExtractFileStep setOutput(File output) {
        this.output = output;
        return this;
    }

    public ExtractFileStep setProject(Project project) {
        this.project = project;
        return this;
    }
}
