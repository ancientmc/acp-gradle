package com.ancientmc.acp.init.step;

import org.gradle.api.logging.Logger;

public class Step {
    protected String message;

    public void printMessage(Logger logger, String message, boolean condition) {
        if (condition) {
            logger.lifecycle(message);
        }
    }

    public void exec() {
        exec(null, true);
    }

    public void exec(Logger logger, boolean condition) {
        printMessage(logger, message, condition);
    }

    public Step setMessage(String message) {
        this.message = message;
        return this;
    }
}
