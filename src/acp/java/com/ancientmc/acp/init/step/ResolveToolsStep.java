package com.ancientmc.acp.init.step;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolveToolsStep extends Step {

    /**
     * The gradle project.
     */
    private Project project;

    /**
     * The gradle properties file, which contains a list of tools ACP uses to deobfuscate and decompile Minecraft.
     */
    private File properties;

    /**
     * Execution method. The properties file is read line-by-line and converted to a hash-map. That map is then used to resolve each tool dependency
     * into its corresponding configuration.
     */
    @Override
    public void exec() {
        try {
            List<String> lines = Files.readAllLines(properties.toPath());
            Map<String, String> map = getConfigMap(lines);
            map.forEach((name, tool) -> {
                Configuration cfg = project.getConfigurations().getByName(name);
                resolve(cfg, tool);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves (adds) the dependency of the given tool configuration to the project.
     * @param cfg The tool configuration.
     * @param tool The name of the tool dependency.
     */
    public void resolve(Configuration cfg, String tool) {
        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            public void beforeResolve(ResolvableDependencies resolvableDependencies) {
                cfg.getDependencies().add(project.getDependencies().create(tool));
                project.getGradle().removeListener(this);
            }

            @Override
            public void afterResolve(ResolvableDependencies resolvableDependencies) { }
        });
    }

    /**
     * The following format is found for the tools listed in the gradle.properties file:
     *          tool_(cfg_name)=(maven_name)
     *          Where cfg_name is the configuration of the tool, and maven_name is the maven path for the tool.
     * @param lines The lines of the gradle.properties file.
     * @return The config map. The key is the configuration name of the tool, while the value is the tool's maven path.
     */
    private Map<String, String> getConfigMap(List<String> lines) {
        Map<String, String> map = new HashMap<>();
        lines.forEach(line -> {
            if (line.contains("tool_")) {
                String[] split = line.split("=");
                String name = split[0].substring(5);
                map.put(name, split[1]);
            }
        });
        return map;
    }

    public ResolveToolsStep setProject(Project project) {
        this.project = project;
        return this;
    }

    public ResolveToolsStep setProperties(File properties) {
        this.properties = properties;
        return this;
    }
}
