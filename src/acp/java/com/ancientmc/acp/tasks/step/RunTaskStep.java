package com.ancientmc.acp.tasks.step;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;

public class RunTaskStep extends Step {
    protected Task task;

    public void exec(Logger logger, boolean condition) {
        task.getActions().forEach(action -> action.execute(task));
    }

    public RunTaskStep setTask(Task task) {
        this.task = task;
        return this;
    }
}
