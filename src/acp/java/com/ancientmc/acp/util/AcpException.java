package com.ancientmc.acp.util;

import com.ancientmc.acp.logger.AcpLogger;
import org.gradle.api.Project;

public class AcpException extends RuntimeException {
    public AcpException(String message, AcpLogger logger, Project project, Throwable throwable) {
        super(message);
        logger.error(project, throwable, message);
    }
}
