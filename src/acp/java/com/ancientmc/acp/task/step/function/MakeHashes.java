package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
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
    protected File sourceDirectory;

    /**
     * The resource directory.
     */
    protected File resourceDirectory;

    protected File output;

    public MakeHashes(Project project, AcpLogger logger, String message) {
        build(project, logger, message);
    }

    @Override
    public void action() {
        try {
            Collection<File> sources = FileUtils.listFiles(sourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
            Collection<File> resources = FileUtils.listFiles(resourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
            write(sources, resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Collection<File> sources, Collection<File> resources) throws IOException {
        Map<String, String> map = new HashMap<>();

        sources.forEach(src -> {
            String hash = getHash(src);
            String name = src.getAbsolutePath();
            System.out.println(name);
            name = name.replace(".class", "")
                    .replace(sourceDirectory.getAbsolutePath() + File.separator, "")
                    .replace(File.separator, "/");
            map.put(name, hash);
        });

        resources.forEach(rs -> {
            String hash = getHash(rs);
            String name = rs.getAbsolutePath();
            System.out.println(name);
            name = name.replace(resourceDirectory.getAbsolutePath() + File.separator, "")
                    .replace(File.separator, "/");
            map.put(name, hash);
        });

        if (!output.getParentFile().exists()) {
            Files.createDirectories(output.getParentFile().toPath());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(Files.readAllBytes(file.toPath()));
            return new BigInteger(1, bytes).toString(16);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
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
