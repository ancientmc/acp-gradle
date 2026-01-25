package com.ancientmc.acp.task.step;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.AcpException;
import com.ancientmc.acp.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MakeHashes extends Step {

    /**
     * The source directory.
     */
    private File sourceDirectory;

    /**
     * The resource directory.
     */
    private File resourceDirectory;

    /**
     * The output hash file.
     */
    private File output;

    public MakeHashes(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        Map<String, String> sources = getHashMap(sourceDirectory);
        Map<String, String> resources = getHashMap(resourceDirectory);
        write(sources, resources);
    }

    public Map<String, String> getHashMap(File directory) {
        Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        Map<String, String> map = new HashMap<>();

        files.forEach(file -> {
            String hash = getHash(file);
            String name = file.getAbsolutePath().replace(".class", "")
                    .replace(directory.getAbsolutePath() + File.separator, "")
                    .replace(File.separator, "/");
            map.put(name, hash);
        });

        return map;
    }

    public void write(Map<String, String> sources, Map<String, String> resources) throws IOException {
        FileUtil.createDirectory(output.getParentFile());
        logger.file(project, "Hash file -> {}", output.getAbsolutePath());

        try (BufferedWriter writer = Files.newBufferedWriter(output.toPath())) {
            for (Map.Entry<String, String> src : sources.entrySet()) {
                writeLine(writer, src);
            }

            for (Map.Entry<String, String> rs : resources.entrySet()) {
                writeLine(writer, rs);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLine(BufferedWriter writer, Map.Entry<String, String> entry) throws IOException {
        logger.file(project, "File -> {}", entry.getKey());
        logger.file(project, "Hash -> {}", entry.getValue());
        writer.write(entry.getKey() + " " + entry.getValue() + "\n");
        writer.flush();
    }

    public String getHash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(Files.readAllBytes(file.toPath()));
            return new BigInteger(1, bytes).toString(16);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new AcpException("Hash generation error: ", logger, project, e);
        }
    }

    public MakeHashes setClassDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        return this;
    }

    public MakeHashes setResourceDirectory(File resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
        return this;
    }

    public MakeHashes setOutput(File output) {
        this.output = output;
        return this;
    }
}
