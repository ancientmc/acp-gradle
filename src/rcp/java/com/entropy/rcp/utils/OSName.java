package com.entropy.rcp.utils;

import org.gradle.internal.os.OperatingSystem;

public class OSName {
    public static String getOSName() {
        OperatingSystem os = OperatingSystem.current();
        if(os.isWindows()) {
            return "windows";
        } else if (os.isMacOsX()) {
            return "osx";
        } else if (os.isLinux() || os.isUnix()) {
            return "linux";
        }
        return "unknown";
    }
}
