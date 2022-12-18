package com.entropy.rcp.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class ExtractNatives extends DefaultTask {
    @TaskAction
    public void extractNatives() throws IOException {
        File nativesDir = getNativesDir().get().getAsFile();
        File[] jars = nativesDir.listFiles();
        assert jars != null;
        for(File jar : jars) {
            extract(jar);
            FileUtils.deleteQuietly(jar);
        }
    }

    public void extract(File file) throws IOException {
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry entry = inputStream.getNextEntry();
        while(entry != null) {
            String output = file.getParent() + File.separator + entry.getName();
            if(!entry.isDirectory()) {
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(output));
                byte[] buffer = new byte[4096];
                int read = 0;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.close();
            } else {
                File subdir = new File(output);
                subdir.mkdir();
            }
            inputStream.closeEntry();
            entry = inputStream.getNextEntry();
        }
        inputStream.close();
    }

    @InputDirectory
    public abstract RegularFileProperty getNativesDir();

}
