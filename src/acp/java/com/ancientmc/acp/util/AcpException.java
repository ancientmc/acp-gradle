package com.ancientmc.acp.util;

import com.ancientmc.acp.logger.AcpLogger;

public class AcpException extends RuntimeException {
    public AcpException(String message, AcpLogger logger, Throwable throwable) {
        super(message);
        logger.error(throwable, message);
    }
}
