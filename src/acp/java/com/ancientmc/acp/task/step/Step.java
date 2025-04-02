package com.ancientmc.acp.task.step;

import org.gradle.api.Project;

/**
 * Base class for steps, which are essentially mini-functions that occur within Gradle tasks.
 */
public abstract class Step {

    /**
     * The message that is printed in the console upon the step's execution.
     */
    protected String message;

    /**
     * The gradle project.
     */
    protected Project project;

    /**
     * The condition that determines if a step is executed.
     */
    protected boolean condition;

    /**
     * Main execution method for all inheritors of the Step class. If the condition is met, the message is printed and the
     * action is performed.
     */
    public void exec() {
        if (condition) {
            project.getLogger().lifecycle(message);
            action();
        }
    }

    /**
     * Performs the main action for this step.
     */
    public abstract void action();

    protected Step build(Project project, String phase, String message) {
        this.project = project;
        this.message = "[acp." + phase + "] Step -> " + message + "...";
        return this;
    }

    public Step setProject(Project project) {
        this.project = project;
        return this;
    }

    public Step setCondition(boolean condition) {
        this.condition = condition;
        return this;
    }
}
