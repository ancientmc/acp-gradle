package com.ancientmc.acp.tasks.step;

import com.ancientmc.acp.util.Paths;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;

import java.util.Arrays;
import java.util.List;

public class JavaCompileStep extends Step {

    /**
     * The Gradle project.
     */
    protected Project project;

    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        TaskProvider<JavaCompile> task = project.getTasks().named("compileJava", JavaCompile.class);
        task.get().getInputs().files(project.fileTree(Paths.DIR_SRC));

        task.configure(compile -> {
            compile.getInputs().files(project.fileTree(Paths.DIR_SRC));
            compile.setSource(compile.getInputs().getFiles());
            compile.setClasspath(project.getExtensions().getByType(SourceSetContainer.class).getByName("main").getCompileClasspath());
            compile.getDestinationDirectory().set(project.file(Paths.DIR_VANILLA_CLASSES));
            compile.setSourceCompatibility("8");
            compile.setTargetCompatibility("8");
            compile.getOptions().setCompilerArgs(List.of("-g:none", "-Xmx3000m"));
            compile.exclude("acp/");
            compile.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        task.get().getActions().forEach(action -> action.execute(task.get()));

    }

    public JavaCompileStep setProject(Project project) {
        this.project = project;
        return this;
    }
}
