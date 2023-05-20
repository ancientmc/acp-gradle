package com.ancientmc.acp.init.step;

import org.gradle.api.logging.Logger;

/**
 * Base class for steps, which are essentially mini-tasks that occur during the initilization (project configuration phase)
 * of ACP's setup.
 */
public class Step {
    /**
     * The message that is printed in the console upon the step's execution.
     */
    protected String message;

    /**
     * Prints the message into the console. Determined by the condition specified.
     * @param logger The Gradle logger.
     * @param message The message getting printed.
     * @param condition Boolean condition that determines if the message is printed.
     */
    public void printMessage(Logger logger, String message, boolean condition) {
        if (condition) {
            logger.lifecycle(message);
        }
    }

    /**
     * Variation of the exec(logger, condition) method for if we want to skip the logging process and conditions.
     * Used during dependency (library) resolution, as that occurs in its own little realm separate from the rest
     * of the initialization stuff.
     */
    public void exec() {
        exec(null, true);
    }

    /**
     * Main execution method for all inheritors of the Step class.
     * @param logger The gradle logger.
     * @param condition Boolean condition that determines if the step gets executed.
     */
    public void exec(Logger logger, boolean condition) {
        printMessage(logger, message, condition);
    }

    public Step setMessage(String message) {
        this.message = message;
        return this;
    }
}
