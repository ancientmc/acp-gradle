package com.ancientmc.acp.logger;

import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;

import java.io.*;
import java.nio.file.Files;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * The custom logger for ACP, partially utilizing Gradle's logging system.
 * @author moist-mason
 */
public class AcpLogger {

    /**
     * The gradle project.
     */
    public Project project;

    /**
     * The log file. A new file is created for every called logger instance.
     */
    public File file;

    /**
     * The file writer.
     */
    public Writer writer;

    /**
     * The execution phase for ACP. Dependent on the task.
     */
    public String phase;

    /**
     * The lines that will be output into the log file.
     */
    public static List<String> lines = new ArrayList<>();

    /**
     * ACP's namespace.
     */
    private static final String NAMESPACE = "acp";

    public AcpLogger(String phase, Project project) {
        this.project = project;
        this.phase = phase;
        this.file = getNewLogFile(phase);
        this.writer = getWriter(file);
    }

    public Writer getWriter(File file) {
        try {
            return new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException("File does not exist.");
        }
    }


    /**
     * Logs a message into the log file only.
     * @param project The gradle project.
     * @param message The log message.
     * @param inputs Any additional inputs that are inserted into the message.
     */
    public void file(Project project, String message, String... inputs) {
        log(project, phase, LogLevel.FILE, message, inputs);
    }

    /**
     * Logs a message into the log file AND the console.
     * @param project The gradle project.
     * @param message The log message.
     * @param inputs Any additional inputs that are inserted into the message.
     */
    public void all(Project project, String message, String... inputs) {
        log(project, phase, LogLevel.ALL, message, inputs);
    }


    /**
     * Logs an error into the log file. Prints the error itself and the stack trace.
     * @param project The gradle project.
     * @param throwable The throwable instance. This will be an IOException almost 95% of the time.
     * @param message The error message.
     */
    public void error(Project project, Throwable throwable, String message) {
        log(project, phase, LogLevel.FILE, message, throwable.getLocalizedMessage());
        log(project, phase, LogLevel.FILE, "Stack trace -> {}", Arrays.toString(throwable.getStackTrace()));
        write();
    }

    private void log(Project project, String phase, LogLevel level, String message, String... data) {
        message = message.replace("{}", String.join(", ", data));
        addToLog(project, phase, message);

        if (!level.isFileOnly()) {
            print(project, message);
        }
    }

    private void print(Project project, String message, Object... objects) {
        project.getLogger().lifecycle(message, objects);
    }

    private void addToLog(Project project, String phase, String data) {
        project.getLogger().info(data);
        lines.add(String.join(" ", bracket(getTime()), bracket(NAMESPACE + "." + phase), data));
    }

    private String bracket(String data) {
        return "[" + data + "]";
    }

    private String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalDate.now().format(formatter);
    }

    private String getDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy.HH-mm-ss");
        return LocalDate.now().format(formatter);
    }

    private File getNewLogFile(String phase) {
        try {
            File parent = project.file(Paths.DIR_LOG);

            if (!parent.exists()) {
                Files.createDirectories(parent.toPath());
            }

            String name = String.join(".", NAMESPACE, phase, getDateTime(), "log");
            File file = new File(parent, name);

            return Files.createFile(file.toPath()).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write() {
        try {
            for (String line : lines) {
                writer.write(line + "\n");
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    enum LogLevel {
        FILE(true),
        ALL(false);

        private final boolean fileOnly;

        LogLevel(boolean fileOnly) {
            this.fileOnly = fileOnly;
        }

        public boolean isFileOnly() {
            return fileOnly;
        }
    }
}
