package com.ancientmc.acp.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.Collection;

/**
 * Injects LZMA files found within a specified directory into the Minecraft JAR.
 * Binary injection is done via Minecraft Forge's Binary Patcher.
 */
public abstract class InjectModLoader extends DefaultTask {

    @TaskAction
    public void exec() {
        File input = getInputJar().get().getAsFile();
        File dir = getPatchDir().get().getAsFile();
        File output = getOutputJar().get().getAsFile();
        run(input, dir, output);
    }

    public void run(File input, File dir, File output) {
        Project project = getProject();
        Collection<File> files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        files.forEach(file -> {
            if(file.getName().endsWith(".lzma")) {
                project.javaexec(action -> {
                    Configuration binpatcher = project.getConfigurations().getByName("binpatcher");
                    action.getMainClass().set("net.minecraftforge.binarypatcher.ConsoleTool");
                    action.setClasspath(project.files(binpatcher));
                    action.args("--clean", input.getAbsolutePath(), "--apply", file.getAbsolutePath(), "--output", output.getAbsolutePath(), "--unpatched");
                });
            }
        });
    }

    /**
     * The input JAR.
     */
    @InputFile
    public abstract RegularFileProperty getInputJar();

    /**
     * The directory containing the LZMA archive(s).
     */
    @InputDirectory
    public abstract RegularFileProperty getPatchDir();

    /**
     * The output JAR.
     */
    @OutputFile
    public abstract RegularFileProperty getOutputJar();
}
