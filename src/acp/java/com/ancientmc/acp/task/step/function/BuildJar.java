package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.task.step.Step;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.jar.JarEntry;

/**
 * Builds the intermediate JAR containing obfuscated class files. The JAR is just an archive so we use Compress-IO to build it.
 */
public class BuildJar extends Step {
    protected Project project;

    protected File classDirectory;

    protected File resourceDirectory;

    protected File output;

    @Override
    public void exec(Logger logger, boolean condition) {
        try {
            if (!output.getParentFile().exists()) {
                Files.createDirectories(output.getParentFile().toPath());
            }

            JarArchiveOutputStream out = new JarArchiveOutputStream(new FileOutputStream(output));
            out.setMethod(8);
            Collection<File> classes = FileUtils.listFiles(classDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
            Collection<File> resources = FileUtils.listFiles(resourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);

            if (!classes.isEmpty()) {
                project.getLogger().lifecycle("Classes size -> " + classes.size());
                classes.forEach(cls -> addEntry(out, classDirectory, cls));
            }

            if (!resources.isEmpty()) {
                project.getLogger().lifecycle("Resources size -> " + classes.size());
                resources.forEach(rs -> addEntry(out, resourceDirectory, rs));
            }

            out.finish();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addEntry(JarArchiveOutputStream out, File directory, File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            String path = getPath(directory, file);
            out.putArchiveEntry(new JarArchiveEntry(new JarEntry(path)));
            IOUtils.copy(in, out);
            out.closeArchiveEntry();
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPath(File directory, File file) {
        return directory.toPath().relativize(file.toPath()).toString();
    }

    public BuildJar setProject(Project project) {
        this.project = project;
        return this;
    }

    public BuildJar setClassDirectory(File classDirectory) {
        this.classDirectory = classDirectory;
        return this;
    }

    public BuildJar setResources(File resources) {
        this.resourceDirectory = resources;
        return this;
    }

    public BuildJar setOutput(File output) {
        this.output = output;
        return this;
    }
}

