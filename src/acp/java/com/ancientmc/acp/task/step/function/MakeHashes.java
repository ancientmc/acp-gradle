package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.AcpException;
import com.ancientmc.acp.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
        build(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        Collection<File> sources = FileUtils.listFiles(sourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        Collection<File> resources = FileUtils.listFiles(resourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        write(sources, resources);
    }

    public void write(Collection<File> sources, Collection<File> resources) throws IOException {
        Map<String, String> map = new HashMap<>();

        sources.forEach(src -> {
            String hash = getHash(src);
            String name = src.getAbsolutePath();
            name = name.replace(".class", "")
                    .replace(sourceDirectory.getAbsolutePath() + File.separator, "")
                    .replace(File.separator, "/");
            map.put(name, hash);
        });

        resources.forEach(rs -> {
            String hash = getHash(rs);
            String name = rs.getAbsolutePath();
            name = name.replace(resourceDirectory.getAbsolutePath() + File.separator, "")
                    .replace(File.separator, "/");
            map.put(name, hash);
        });

        FileUtil.createDirectory(output.getParentFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            logger.file(project, "Hash file -> {}", output.getAbsolutePath());

            for (Map.Entry<String, String> entry : map.entrySet()) {
                logger.file(project, "File -> {}", entry.getKey());
                logger.file(project, "Hash -> {}", entry.getValue());
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            throw new AcpException("File writing error: ", logger, project, e);
        }
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
