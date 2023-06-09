package com.ancientmc.acp.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Repackages default java source files into the "net/minecraft/src/" package.
 */
public abstract class RepackageDefaults extends DefaultTask {

    @TaskAction
    public void exec() {
        try {
            File in = getSourceDirIn().getAsFile().get();
            File out = getSourceDirOut().getAsFile().get();

            File[] files = in.listFiles((File file) -> file.getName().endsWith(".java") && !file.isDirectory());
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();

                    // Creates a temp file that we will add the package header to.
                    File temp = getProject().file(out.getPath() + "\\temp-" + name);
                    writeFile(file, temp, "package net.minecraft.src;\n\n");

                    // Moves the temp file to the endpoint path in net/minecraft/src, and then delete the temp file.
                    File newFile = getProject().file(out.getPath() + "\\net\\minecraft\\src\\" + name);
                    writeFile(temp, newFile, "");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple method to write a new file based on an old file (which gets deleted), with the ability to addadditional text at the beginning.
     * @param in The input file.
     * @param out The output file.
     * @param toAdd The text getting added.
     * @throws IOException
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

    /**
     * The source directory.
     */
    @InputDirectory
    public abstract RegularFileProperty getSourceDirIn();

    /**
     * The source directory. It's the same as the input directory, they're only notated differently
     * to prevent IO jank.
     */
    @OutputDirectory
    public abstract RegularFileProperty getSourceDirOut();
}
