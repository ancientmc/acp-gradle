package com.ancientmc.acp.utils;

import org.gradle.internal.os.OperatingSystem;

import java.io.File;

/**
 * Gets a shortened version of the operating system's name. This class is used in getting the native URLs,
 * as different versions for LWJGL's natives are needed depending on the operating system.
 * @see Json#getNativeUrls(File) 
 */
public class OSName {
    public static String get() {
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
