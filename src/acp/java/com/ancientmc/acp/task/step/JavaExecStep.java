package com.ancientmc.acp.task.step;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Step used to execute Java-based tools needed during decompilation.
 */
public class JavaExecStep extends Step {

    /**
     * The configuration for our tool being executed.
     */
    protected Configuration configuration;

    /**
     * The main class of the tool being executed.
     */
    protected String mainClass;

    /**
     * The tool's command line arguments.
     */
    protected List<String> args;

    public JavaExecStep(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        OutputStream out = new ByteArrayOutputStream();

        project.javaexec(action -> {
            action.setClasspath(project.files(configuration));
            action.getMainClass().set(mainClass);
            action.setArgs(args);

            if (isMakeDiffPatchesStep(args)) {
                action.setIgnoreExitValue(true);
            }

            action.setStandardOutput(out);
        });

        out.flush();
        out.close();
        log(configuration.getName(), out.toString());
    }

    public void log(String tool, String out) {
        logger.file(project, "Tool -> {}", tool);
        logger.file(project, "Output ->");
        List<String> elements = Arrays.asList(out.split("\n"));
        elements.forEach(e -> logger.file(project, "\t\t" + e));
    }

    public JavaExecStep setConfiguration(String configuration) {
        this.configuration = project.getConfigurations().named(configuration).get();
        return this;
    }

    public JavaExecStep setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public JavaExecStep setArgs(List<String> args) {
        this.args = args;
        return this;
    }

    /**
     * The makeDiffPatches step as part of the BuildMod task crashes the process unless we ignore the exit value. We don't want to do this
     * for all JavaExec steps, so for each JavaExec we check for the args, and only call the JavaExec.setIgnoreExitValue method if there's a match
     * with the makeDiffPatches step.
     */
    public static boolean isMakeDiffPatchesStep(List<String> args) {
        return args.equals(Arrays.asList("--diff", Paths.DIR_VANILLA_SRC, Paths.DIR_SRC, "--output", Paths.DIR_MODDED_PATCHES + "/diff/"));
    }
}
