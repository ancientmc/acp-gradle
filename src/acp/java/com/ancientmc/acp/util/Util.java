package com.ancientmc.acp.util;

import net.neoforged.srgutils.IMappingFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class full of miscellaneous utilities.
 */
public class Util {

    /**
     * Gets the minecraft version from the version property.
     */
    public static String getMinecraftVersion(Project project) {
        String versionProperty = project.getProperties().get("minecraft_version").toString();
        return versionProperty.equals("a1.2.6-legacy") ? "a1.2.6" : versionProperty;
    }

    /**
     * Gets a map of class names from the SRG file. The key is the obfuscated name, while the value is the mapped name.
     * @param srg The SRG file.
     * @return The class map.
     * @throws IOException exception
     */
    public static Map<String, String> getClassMap(File srg) throws IOException {
        Map<String, String> map = new HashMap<>();
        IMappingFile mapping = IMappingFile.load(srg);
        mapping.getClasses().forEach(cls -> map.put(cls.getOriginal(), cls.getMapped()));
        return map;
    }

    /**
     * Converts a maven path into a URL whose contents can be downloaded.
     * @param repo The repository URL.
     * @param path The maven path (group.sub:name:version).
     * @param ext The file extension.
     * @return The maven URL.
     */
    public static URL toMavenUrl(String repo, String path, String ext) throws IOException {
        String[] split = path.split(":");
        String file = split[1] + "-" + split[2] + (split.length > 3 ? "-" + split[3] : "") + "." + ext;
        String newPath = split[0].replace('.', '/') + "/" + split[1] + "/" + split[2] + "/" + file;
        return Util.getUrl(repo + newPath);
    }

    /**
     * Quick method that compresses multiple files into both a ZIP and TAR
     * @param files The mod file map.
     * @param directory The output directory.
     * @throws IOException exception
     */
    public static void compress(Map<File, String> files, File directory, String version) throws IOException {
        String archive = directory.getName();
        Util.compressZip(files, new File(directory, archive + "-" + version + ".zip"));
        Util.compressTar(files, new File(directory, archive + "-" + version + ".tar.gz"));
    }

    /**
     * Simple ZIP compression function for mod files.
     * @param files The mod file map.
     * @param zip The output ZIP.
     * @throws IOException exception
     */
    public static void compressZip(Map<File, String> files, File zip) throws IOException {
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
    public static void compressTar(Map<File, String> files, File tar) throws IOException {
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
     * Gets a shortened version of the operating system's name. This class is used in getting the native URLs,
     * as different versions for LWJGL's natives are needed depending on the operating system.
     * @see Json#getNativeUrls(File)
     */
    public static String getOSName() {
        OperatingSystem os = OperatingSystem.current();

        if (os.isWindows()) {
            return "windows";
        } else if (os.isMacOsX()) {
            return "osx";
        } else if (os.isLinux() || os.isUnix()) {
            return "linux";
        }
        return "unknown";
    }

    /**
     * Returns a URL from a String path.
     * @param path The path for the URL.
     * @return The URL.
     */
    public static URL getUrl(String path) {
        try {
            return URI.create(path).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean directoryCondition(File directory) {
        return !directory.exists() || isDirectoryEmpty(directory);
    }

    public static boolean isDirectoryEmpty(File directory) {
        Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        return files.isEmpty();
    }

    /**
     * @return the URL to AncientMC's maven as a string.
     */
    public static String getAncientMCMaven() {
        return "https://github.com/ancientmc/ancientmc-maven/raw/maven/";
    }
}
