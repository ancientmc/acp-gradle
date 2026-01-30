package com.ancientmc.acp.task.step;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.FileUtil;
import net.neoforged.srgutils.IMappingFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MakeArchives extends Step {

    /**
     * The TSRG file.
     */
    private File srg;

    /**
     * The directory containing our obfuscated class files (build/modding/reobfClasses/)
     */
    private File obfDirectory;

    /**
     * The directory containing our resources (src/main/resources/).
     */
    private File resourceDirectory;

    /**
     * The directory containing our hash files.
     */
    private File hashDirectory;

    /**
     * The directory where the ZIP and TAR archives get put into.
     */
    private File archiveDirectory;

    public MakeArchives(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        FileUtil.createDirectory(archiveDirectory);

        Map<String, String> vanillaMap = getHashMap(new File(hashDirectory, "vanilla.md5"));
        Map<String, String> moddedMap = getHashMap(new File(hashDirectory, "modded.md5"));
        Map<String, String> classMap = getClassMap(srg);
        String version = project.getVersion().toString(); // Mod version -> version in build.gradle or somewhere else defined by the end-user.
        Map<File, String> moddedFiles = new HashMap<>(); // key -> the file. value -> the file path.

        // Remove ACP start classes from map.
        List<Map.Entry<String, String>> entries = moddedMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith("acp/client/")).toList();
        entries.forEach(moddedMap.entrySet()::remove);

        moddedMap.forEach((name, hash) -> {
            if (!vanillaMap.containsValue(hash)) {
                if (name.startsWith("net/minecraft/") || (name.startsWith("com/mojang"))) {

                    // For non-Minecraft classes, get the name without the package.
                    String strippedName = name.substring(name.lastIndexOf('/') + 1);

                    String className = classMap.containsValue(name) ? classMap.get(name) : strippedName;
                    File moddedClass = project.file(obfDirectory.getPath() + "/" + className + ".class");
                    moddedFiles.put(moddedClass, className + ".class");

                } else {

                    // Add resources
                    File moddedResource = project.file(resourceDirectory.getPath() + "/" + name);
                    moddedFiles.put(moddedResource, name);
                }
            }
        });

        String archive = archiveDirectory.getName();
        compressZip(moddedFiles, new File(archiveDirectory, archive + "-" + version + ".zip"));
        compressTar(moddedFiles, new File(archiveDirectory, archive + "-" + version + ".tar.gz"));
    }

    /**
     * Gets a map of the text file containing the hashes.
     * The key is the class name, while the value is the hash.
     */
    public static Map<String, String> getHashMap(File hashFile) throws IOException {
        Map<String, String> map = new HashMap<>();
        List<String> lines = FileUtils.readLines(hashFile, StandardCharsets.UTF_8);

        lines.forEach(line -> {
            String[] split = line.split(" ");

            // split[0] = class name; split[1] = hash
            map.put(split[0], split[1]);
        });

        return map;
    }

    /**
     * Gets a map of class names from the SRG file. The key is the mapped name, while the value is the obfuscated name.
     * @param srg The SRG file.
     * @return The class map.
     * @throws IOException exception
     */
    public static Map<String, String> getClassMap(File srg) throws IOException {
        Map<String, String> map = new HashMap<>();
        IMappingFile mapping = IMappingFile.load(srg);
        mapping.getClasses().forEach(cls -> map.put(cls.getMapped(), cls.getOriginal()));
        return map;
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

    public MakeArchives setSrg(File srg) {
        this.srg = srg;
        return this;
    }

    public MakeArchives setObfDirectory(File obfDirectory) {
        this.obfDirectory = obfDirectory;
        return this;
    }

    public MakeArchives setResourceDirectory(File resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
        return this;
    }

    public MakeArchives setHashDirectory(File hashDirectory) {
        this.hashDirectory = hashDirectory;
        return this;
    }

    public MakeArchives setArchiveDirectory(File archiveDirectory) {
        this.archiveDirectory = archiveDirectory;
        return this;
    }
}
