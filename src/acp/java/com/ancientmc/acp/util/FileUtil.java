package com.ancientmc.acp.util;

import com.ancientmc.acp.logger.AcpLogger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for file-related functions.
 * @author moist-mason
 */
public class FileUtil {

    /**
     * Quick method that compresses multiple files into both a ZIP and TAR
     * @param files The mod file map.
     * @param directory The output directory.
     * @throws IOException exception
     */
    public static void compress(Map<File, String> files, File directory, String version) throws IOException {
        String archive = directory.getName();
        compressZip(files, new File(directory, archive + "-" + version + ".zip"));
        compressTar(files, new File(directory, archive + "-" + version + ".tar.gz"));
    }

    /**
     * Simple ZIP compression function for mod files.
     * @param files The mod file map.
     * @param zip The output ZIP.
     * @throws IOException exception
     */
    private static void compressZip(Map<File, String> files, File zip) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zip.toPath()));

        files.forEach((file, path) -> {
            try {
                zipOut.putNextEntry(new ZipEntry(path));
                FileInputStream in = new FileInputStream(file);
                IOUtils.copy(in, zipOut);
                zipOut.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        zipOut.close();
    }

    /**
     * Simple TAR/GZIP compression function for mod files.
     * @param files The mod file map.
     * @param tar The output TAR GZIP.
     * @throws IOException exception
     */
    private static void compressTar(Map<File, String> files, File tar) throws IOException {
        GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(Files.newOutputStream(tar.toPath()));
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut);

        files.forEach((file, name) -> {
            try {
                tarOut.putArchiveEntry(new TarArchiveEntry(file, name));
                FileInputStream in = new FileInputStream(file);
                IOUtils.copy(in, tarOut);
                tarOut.closeArchiveEntry();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        tarOut.finish();
        tarOut.close();
        gzipOut.close();
    }


    /**
     * Downloads a file from a URL, and creates the parent directory if needed.
     * @param input The source URL.
     * @param output The target file.
     * @throws IOException exception.
     */
    public static void download(AcpLogger logger, URL input, File output) throws IOException {
        createDirectory(output.getParentFile());
        logger.functions().download(input, output);
        FileUtils.copyURLToFile(input, output);
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

    /**
     * Checks if a directory exists, and creates it if not.
     * @param directory The directory being created.
     * @throws IOException exception
     */
    public static void createDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            Files.createDirectories(directory.toPath());
        }
    }

    /**
     * @param directory The input directory.
     * @return true if the directory either does not exist or its contents are empty.
     */
    public static boolean directoryCondition(File directory) {
        return !directory.exists() || isDirectoryEmpty(directory);
    }

    /**
     * @param directory The input directory.
     * @return true if the directory is empty.
     */
    private static boolean isDirectoryEmpty(File directory) {
        Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        return files.isEmpty();
    }
}