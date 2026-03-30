package com.ancientmc.acp.logger;

import com.ancientmc.acp.util.FileUtil;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class AcpLogger {

    /** Adds square brackets [] around a string. **/
    private static final Function<String, String> BRACKET = (data) -> "[" + data + "]";

    /** The Gradle project. */
    private final Project project;

    /** The execution phase for ACP. Dependent on the Task. */
    private final String phase;

    /** The log file. **/
    private final LogFile file;

    public AcpLogger(final Project project, final String phase) {
        this.project = project;
        this.phase = phase;
        this.file = new LogFile(phase, project, getTime("dd-MM-yy.HH-mm-ss-SSS"));
    }

    public LogFile getFile() {
        return file;
    }

    /**
     * Logs a message into the log file only.
     * @param message The log message.
     * @param inputs Any additional inputs that are inserted into the message.
     */
    public void toFile(String message, String... inputs) {
        log(false, message, inputs);
    }

    /**
     * Logs a message into the log file and the console.
     * @param message The log message.
     * @param inputs Any additional inputs that are inserted into the message.
     */
    public void toConsole(String message, String... inputs) {
        log(true, message, inputs);
    }

    /**
     * Logs an error into the log file. Prints the error itself and the stack trace.
     * @param throwable The throwable instance. This will be an IOException almost 95% of the time.
     * @param message The error message.
     */
    public void error(Throwable throwable, String message) {
        // TODO: Works, but make more tests.
        List<String> elements = formatStackTrace(throwable.getStackTrace());

        log(true, message, throwable.getLocalizedMessage());
        log(true, "Stacktrace:");

        for (String e : elements) {
            log(true, e);
        }

        file.write();
    }

    private List<String> formatStackTrace(StackTraceElement[] elements) {
        List<String> list = new LinkedList<>();
        for (StackTraceElement e : elements) {
            list.add(e.toString());
        }

        return list;
    }

    public LogFunctions functions() {
        return new LogFunctions(this);
    }

    private void log(boolean console, String message, String... data) {
        message = message.replace("{}", String.join(", ", data));

        if (console) { // The "Step -> " execution messages and the ListSuppportedVersions task output get printed to the console.
            message = BRACKET.apply("acp." + phase) + " " + message;
            project.getLogger().lifecycle(message);
        } else {
            message = "\t\t" + message; // non-console messages are indented twice.
        }

        String logTime = getTime("HH:mm:ss.SSS");
        file.add(logTime, message);
    }

    private String getTime(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Representation of the log file.
     * @param phase The execution phase.
     * @param project The Gradle project.
     * @param time The time of the log file's creation.
     */
    public record LogFile(String phase, Project project, String time) {
        private static final List<LogLine> LINES = new LinkedList<>();

        public void add(String time, String data) {
            project.getLogger().info(data);
            LINES.add(new LogLine(phase, time, data));
        }

        private Path toPath() throws IOException {
            File parent = project.file(Paths.DIR_LOG + "/" + phase);
            FileUtil.createDirectory(parent);

            String name = String.join(".", "acp", phase, time, "log");
            Path path = new File(parent, name).toPath();
            return Files.createFile(path);
        }

        public void write() {
            try (BufferedWriter writer = Files.newBufferedWriter(toPath())) {
                for (LogLine line : LINES) {
                    writer.write(line.toString());
                    writer.flush();
                }

                LINES.clear(); // This list is static, so clear the line cache for next time.
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Representation of a line in the log file.
     * @param phase The execution phase.
     * @param time The time the line is logged.
     * @param data The data for this line.
     */
    public record LogLine(String phase, String time, String data) {

        @Override
        public @NotNull String toString() {
            return String.join(" ",
                    BRACKET.apply(time),
                    BRACKET.apply("acp." + phase),
                    data + "\n");
        }
    }
}
