package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.task.step.Step;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class RepackageDefaults extends Step {

    /**
     * The source directory.
     */
    protected File inputDirectory;

    /**
     * The source directory. It's the same as the input directory, they're only notated differently
     * to prevent IO jank.
     */
    protected File outputDirectory;

    /**
     * The gradle project.
     */
    protected Project project;

    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        try {
            File[] files = inputDirectory.listFiles((File file) -> file.getName().endsWith(".java") && !file.isDirectory());

            if (files != null) {
                for (File file : files) {
                    String name = file.getName();

                    // Creates a temp file that we will add the package header to.
                    File temp = project.file(outputDirectory.getPath() + "/temp-" + name);
                    writeFile(file, temp, "package net.minecraft.src;\n\n");

                    // Moves the temp file to the endpoint path in net/minecraft/src, and then deletes the temp file.
                    File newFile = project.file(outputDirectory.getPath() + "/net/minecraft/src/" + name);
                    writeFile(temp, newFile, "");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simple method to write a new file based on an old file (which gets deleted), with the ability to add additional text at the beginning.
     * @param in The input file.
     * @param out The output file.
     * @param toAdd The text getting added.
     * @throws IOException exception.
     */
    public void writeFile(File in, File out, String toAdd) throws IOException {
        List<String> lines = Files.readAllLines(in.toPath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(out))) {
            writer.write(toAdd);
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
        FileUtils.forceDelete(in);
    }

    public RepackageDefaults setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
        return this;
    }

    public RepackageDefaults setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }
}
