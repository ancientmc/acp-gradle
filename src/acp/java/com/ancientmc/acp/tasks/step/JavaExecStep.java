package com.ancientmc.acp.tasks.step;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;

import java.util.List;

/**
 * Step used to execute Java-based programs during decompilation.
 */
public class JavaExecStep extends Step {

    /**
     * The gradle project.
     */
    protected Project project;

    /**
     * The configuration for our tool.
     */
    protected Configuration configuration;

    /**
     * The main class of the tool.
     */
    protected String mainClass;

    /**
     * The tool's Java arguments.
     */
    protected List<String> args;

    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        project.javaexec(action -> {
            action.setClasspath(project.files(configuration));
            action.getMainClass().set(mainClass);
            action.setArgs(args);
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
}
