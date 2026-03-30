package com.ancientmc.acp.util;

import com.ancientmc.acp.logger.AcpLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for file-related functions.
 * @author moist-mason
 */
public final class FileUtil {

    public static void download(AcpLogger logger, URL input, File output) throws IOException {
        createDirectory(output.getParentFile());
        FileUtils.copyURLToFile(input, output);
        logger.functions().download(input, output);
    }

    public static void copy(Project project, AcpLogger logger, Object input, File output, List<String> exclusions) throws IOException {
        logger.functions().copy((File) input, output);
        copy(project, input, output, null, exclusions);
    }

    private static void copy(Project project, Object input, File output, List<String> inclusions, List<String> exclusions) throws IOException {
        if (output.isDirectory()) {
            createDirectory(output);
        }

        project.copy(c -> {
            c.from(input);
            c.into(output);

            if (inclusions != null) {
                c.include(inclusions);
            }

            if (exclusions != null) {
                c.exclude(exclusions);
            }
        });
    }

    public static void extract(Project project, AcpLogger logger, File archive, File target) throws IOException {
        logger.functions().extract(archive, target);
        copy(project, project.zipTree(archive), target, null, null);
    }

    public static void extract(Project project, AcpLogger logger, File archive, File target, List<String> inclusions, List<String> exclusions) throws IOException {
        logger.functions().extract(archive, target);
        copy(project, project.zipTree(archive), target, inclusions, exclusions);
    }

    public static void createDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            Files.createDirectories(directory.toPath());
        }
    }

    /**
     * @param directory The input directory.
     * @return true if the directory either does not exist or its contents are empty.
     */
    public static boolean isDirectoryEmpty(File directory) {
        Collection<File> files = directoryTree(directory);
        return !directory.exists() || files.isEmpty();
    }

    public static Collection<File> directoryTree(File directory) {
        return FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
    }
}