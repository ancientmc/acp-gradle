package com.ancientmc.acp.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
     * Checks if a project configuration is empty, i.e. if its dependencies haven't been installed during the configuration phase.
     * @param project The gradle project.
     * @param name The name of the configuration.
     * @return true if the configuration(s) is empty.
     */
    public static boolean dependencyCondition(Project project, String name) {
        Configuration cfg = project.getConfigurations().named(name).get();
        return cfg.getDependencies().isEmpty();
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
