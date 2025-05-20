package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

/**
 * Prints the startup message upon ACP's initialization.
 */
public class StartupMessage extends Step {

    /**
     * The Minecraft version.
     */
    private final String minecraftVersion;

    public StartupMessage(Project project, String minecraftVersion, AcpLogger logger) {
        this.logger = logger;
        this.project = project;
        this.minecraftVersion = minecraftVersion;
        this.message = getStartupMessage();
    }

    @Override
    public void action() { } // blank because nothing besides the message printing actually happens.

    /**
     * @return The startup message used upon booting the ACP initializer for the first time.
     */
    private String getStartupMessage() {
        List<String> lines = Arrays.asList("\nAncient Coder Pack",
                "Copyright (c) AncientMC",
                "ACP Version: " + project.getProperties().get("acp_version").toString(),
                "ACP-Gradle Version: " + getPluginVersion(),
                "Minecraft Version: " + minecraftVersion
        );
        return String.join("\n", lines) + "\n";
    }

    /**
     * @return The ACP Gradle version by parsing the plugin's JAR manifest.
     */
    private String getPluginVersion() {
        try {
            Plugin<?> plugin = project.getPlugins().stream().filter(p -> p.getClass().getName().contains("acp")).findAny().orElse(null);

            if (plugin != null) {
                URLClassLoader loader = (URLClassLoader) plugin.getClass().getClassLoader();
                Manifest manifest = new Manifest(loader.findResource("META-INF/MANIFEST.MF").openStream());
                return manifest.getMainAttributes().getValue("Implementation-Version");
            }

            return "unknown";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
