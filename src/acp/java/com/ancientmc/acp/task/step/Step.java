package com.ancientmc.acp.task.step;

import com.ancientmc.acp.logger.AcpLogger;
import org.gradle.api.Project;

/**
 * Base class for steps, which are essentially mini-functions that occur within Gradle tasks.
 * @author moist-mason
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
     * ACP's logger.
     */
    protected AcpLogger logger;

    /**
     * Main execution method for all inheritors of the Step class. If the condition is met, the message is printed and the
     * action is performed.
     */
    public void exec() {
        if (condition) {
            logger.all(project, message);
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

    protected Step build(Project project, AcpLogger logger, String message) {
        this.project = project;
        this.logger = logger;
        this.message = "[acp." + logger.phase + "] Step -> " + message + "...";
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
