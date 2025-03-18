package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Step used to execute Java-based tools needed during decompilation.
 */
public class JavaExecStep extends Step {

    /**
     * The gradle project.
     */
    protected Project project;

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

    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        project.javaexec(action -> {
            action.setClasspath(project.files(configuration));
            action.getMainClass().set(mainClass);
            action.setArgs(args);

            if (isMakeDiffPatchesStep(args)) {
                action.setIgnoreExitValue(true);
            }
        });
    }

    public JavaExecStep setProject(Project project) {
        this.project = project;
        return this;
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
