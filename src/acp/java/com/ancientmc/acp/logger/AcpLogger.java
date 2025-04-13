package com.ancientmc.acp.logger;

import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
    public List<String> lines;

    /**
     * ACP's namespace.
     */
    private static final String NAMESPACE = "acp";

    public AcpLogger(String phase, Project project) {
        this.project = project;
        this.phase = phase;
        this.file = getNewLogFile(phase);
        this.lines = new ArrayList<>();
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
     * Logs a message into the log file.
     * @param project The gradle project.
     * @param message The log message.
     * @param inputs Any additional inputs that are inserted into the message.
     */
    public void file(Project project, String message, String... inputs) {
        log(project, phase, LogLevel.ALL, message, inputs);
    }

    /**
     * Prints a message to the console.
     * @param project The gradle project.
     * @param message The log message.
     * @param inputs Any additional inputs that are inserted into the message.
     */
    public void console(Project project, String message, String... inputs) {
        log(project, phase, LogLevel.CONSOLE, message, inputs);
    }

    /**
     * Logs an error into the log file. Prints the error itself and the stack trace.
     * @param project The gradle project.
     * @param throwable The throwable instance. This will be an IOException almost 95% of the time.
     * @param message The error message.
     */
    public void error(Project project, Throwable throwable, String message) {
        log(project, phase, LogLevel.CONSOLE, message, throwable.getLocalizedMessage());
        log(project, phase, LogLevel.CONSOLE, "Stack trace -> {}", Arrays.toString(throwable.getStackTrace()));
        write();
    }

    public LogFunctions functions() {
        return new LogFunctions(this, project);
    }

    private void log(Project project, String phase, LogLevel level, String message, String... data) {
        message = message.replace("{}", String.join(", ", data));

        if (level.inConsole()) {
            print(project, phase, message);
        } else {
            message = "\t\t" + message; // Log file-only messages are indented twice.
        }

        addToLog(project, phase, message);
    }

    // The only thing printed to the console are the messages saying if a step has been executed.
    // The messages are formatted to include phase information ([acp.{phase}]).
    private void print(Project project, String phase, String message) {
        message = bracket(NAMESPACE + "." + phase) + " " + message;
        project.getLogger().lifecycle(message);
    }

    private void addToLog(Project project, String phase, String data) {
        project.getLogger().info(data);
        lines.add(String.join(" ", bracket(getTime()), bracket(NAMESPACE + "." + phase), data));
    }


    /**
     * @param data a String.
     * @return That string surrounded by brackets.
     */
    private String bracket(String data) {
        return "[" + data + "]";
    }

    private String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.now().format(formatter);
    }

    private String getDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy.HH-mm-ss");
        return LocalDateTime.now().format(formatter);
    }

    private File getNewLogFile(String phase) {
        try {
            File parent = project.file(Paths.DIR_LOG + "/" + phase);

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
        CONSOLE(true),
        ALL(false);

        private final boolean consoleOnly;

        LogLevel(boolean console) {
            this.consoleOnly = console;
        }

        public boolean inConsole() {
            return consoleOnly;
        }
    }
}
