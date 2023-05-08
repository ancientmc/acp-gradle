package com.ancientmc.acp.init.step;

import org.gradle.api.logging.Logger;

public abstract class Step {


    public void printMessage(Logger logger, String message, boolean condition) {
        if (condition) {
            logger.lifecycle(message);
        }
    }

    public abstract void exec();
}
