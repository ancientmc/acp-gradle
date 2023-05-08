package com.ancientmc.acp.init.step;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadJarStep extends DownloadFileStep {
    private String version;

    @Override
    public void exec() {
        try {
            File jar = new File(output, version + (input.getPath().contains("client") ? ".jar" : "-server.jar"));
            FileUtils.copyURLToFile(input, jar);
        } catch (IOException e) {
            e.printStackTrace();
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
