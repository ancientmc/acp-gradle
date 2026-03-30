package com.ancientmc.acp.util;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Class full of miscellaneous utilities.
 * @author moist-mason
 */
public final class Util {

    /**
     * Gets the minecraft version from the version property. If the developer is using the legacy Alpha 1.2.6 build,
     * "a1.2.6" is the returned string.
     * @param project The Gradle project.
     */
    public static String getMinecraftVersion(Project project) {
        String versionProperty = project.getProperties().get("minecraft_version").toString();
        return versionProperty.equals("a1.2.6-legacy") ? "a1.2.6" : versionProperty;
    }

    /**
     * Converts a maven path into a URL whose contents can be downloaded.
     * @param repo The repository URL.
     * @param path The maven path (group.sub:name:version).
     * @param ext The file extension.
     * @return The maven URL.
     */
    public static URL toMavenUrl(String repo, String path, String ext) {
        String[] split = path.split(":");
        String file = split[1] + "-" + split[2] + (split.length > 3 ? "-" + split[3] : "") + "." + ext;
        String newPath = split[0].replace('.', '/') + "/" + split[1] + "/" + split[2] + "/" + file;
        return getUrl(repo + newPath);
    }

    /**
     * Checks if a project configuration is empty, i.e. if its dependencies haven't been installed during the
     * configuration phase.
     * @param project The Gradle project.
     * @param name The name of the configuration.
     * @return true if the configuration(s) is empty.
     */
    public static boolean areDependenciesPresent(Project project, String name) {
        Configuration cfg = project.getConfigurations().named(name).get();
        return cfg.getDependencies().isEmpty();
    }

    /**
     * Returns a URL from a String path.
     * @param path The path for the URL.
     * @return The URL.
     */
    public static URL getUrl(String path) {
        try {
            return URI.create(path).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the URL to AncientMC's maven as a string.
     */
    public static String getAncientMcMaven() {
        return "https://github.com/ancientmc/ancientmc-maven/raw/maven/";
    }
}
