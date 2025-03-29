package com.ancientmc.acp.logging;

import org.gradle.api.Project;

import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class AcpLogger {
    public final File file;
    public final String namespace;
    public Writer writer;
    public static List<String> lines;

    public AcpLogger(File file, String namespace) {
        this.file = file;
        this.namespace = namespace;
        this.writer = getWriter(file);
    }

    public Writer getWriter(File file) {
        try {
            return new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException("File does not exist.");
        }
    }

    public void log(Project project, String phase, LogLevel level, String data) {
        print(project, data);

        if (!level.isConsoleOnly()) {
            addToLog(project, phase, data);
        }
    }

    public void print(Project project, String data) {
        project.getLogger().lifecycle(data);
    }

    public void addToLog(Project project, String phase, String data) {
        project.getLogger().info(data);
        lines.add(String.join(" ", getTime(), getCombinedPhase(phase), data));
    }
    
    public String getCombinedPhase(String phase) {
        return "[" + namespace + phase + "]";
    }

    public String getTime() {
        Format format = new SimpleDateFormat("HH:mm:ss");
        return "[" + format.format(Calendar.getInstance().getTime()) + "]";
    }
}
