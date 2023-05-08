package com.ancientmc.acp.init.step;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadFileStep extends Step {
    protected URL input;
    protected File output;

    @Override
    public void exec() {
        try {
            FileUtils.copyURLToFile(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public URL getInput() {
        return input;
    }

    public File getOutput() {
        return output;
    }

    public DownloadFileStep setInput(URL input) {
        this.input = input;
        return this;
    }

    public DownloadFileStep setOutput(File output) {
        this.output = output;
        return this;
    }
}