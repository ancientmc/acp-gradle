package com.ancientmc.acp.task.step.common;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.AcpException;
import com.ancientmc.acp.util.FileUtil;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;

/**
 * Builds the intermediate JAR containing obfuscated class files. The JAR is just an archive so we use Compress-IO to
 * build it.
 * @author moist-mason
 */
public class BuildJar extends Step {

    /** The class directory. */
    private File classDirectory;

    /** The resource directory. */
    private File resourceDirectory;

    /** The output JAR. */
    private File output;

    public BuildJar(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        FileUtil.createDirectory(output.getParentFile());

        logger.toFile("Class directory -> {}", classDirectory.getAbsolutePath());
        logger.toFile("Resource directory -> {}", resourceDirectory.getAbsolutePath());
        logger.toFile("Output -> {}", output.getAbsolutePath());

        JarArchiveOutputStream out = new JarArchiveOutputStream(new FileOutputStream(output));
        Collection<File> classes = FileUtils.listFiles(classDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        Collection<File> resources = FileUtils.listFiles(resourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);

        if (!classes.isEmpty()) {
            classes.forEach(cls -> addEntry(out, classDirectory, cls));
        }

        if (!resources.isEmpty()) {
            resources.forEach(rs -> addEntry(out, resourceDirectory, rs));
        }

        out.finish();
        out.close();
    }

    // TODO: make this look less like shit. Can any other library do this in less lines?
    public void addEntry(JarArchiveOutputStream out, File directory, File file) {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            String path = getPath(directory, file);
            logger.toFile("Entry -> {}", path);
            out.putArchiveEntry(new JarArchiveEntry(path));
            IOUtils.copy(in, out);
            in.close();
            out.closeArchiveEntry();
            out.flush();
        } catch (IOException e) {
            throw new AcpException(e.getMessage(), logger, e);
        }
    }

    public static String getPath(File directory, File file) {
        return directory.toPath().relativize(file.toPath()).toString();
    }

    public BuildJar setClassDirectory(File classDirectory) {
        this.classDirectory = classDirectory;
        return this;
    }

    public BuildJar setResourceDirectory(File resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
        return this;
    }

    public BuildJar setOutput(File output) {
        this.output = output;
        return this;
    }
}
