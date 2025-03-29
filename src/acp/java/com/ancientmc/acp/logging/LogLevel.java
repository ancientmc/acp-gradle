package com.ancientmc.acp.logging;

public enum LogLevel {
    CONSOLE(true),
    ALL(true);

    private final boolean consoleOnly;

    LogLevel(boolean consoleOnly) {
        this.consoleOnly = consoleOnly;
    }

    public boolean isConsoleOnly() {
        return consoleOnly;
    }
}
