package com.ancientmc.acp.util;

import java.util.List;
import java.util.Locale;

/**
 * Operating systems.
 * @author moist-mason
 */
public enum Os {
    LINUX("linux"),
    MACOS("osx"),
    WINDOWS("windows"),
    UNKNOWN("unknown");

    private final String name;

    Os(String name) {
        this.name = name;
    }

    /** @return The currently installed OS holding the project workspace. */
    public static Os current() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        List<String> commonDistros = List.of("debian", "ubuntu", "mint", "arch", "fedora", "zorin");

        // similar to NeoFormRuntime, OsType.java
        // in turn similar to Apache Commons Lang 3, SystemUtils.java
        if (osName.startsWith("linux") || commonDistros.stream().anyMatch(osName::contains)) {
            return LINUX;
        } else if (osName.startsWith("mac") || osName.contains("osx") || osName.contains("os x")) {
            return MACOS;
        } else if (osName.startsWith("windows")) {
            return WINDOWS;
        } else {
            return UNKNOWN;
        }
    }

    public String getName() {
        return this.name;
    }
}
