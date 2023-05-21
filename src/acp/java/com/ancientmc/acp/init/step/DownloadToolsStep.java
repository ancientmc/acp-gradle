package com.ancientmc.acp.init.step;

import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadToolsStep extends Step {
    private Project project;

    private File properties;

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

    public DownloadToolsStep setProject(Project project) {
        this.project = project;
        return this;
    }

    public DownloadToolsStep setProperties(File properties) {
        this.properties = properties;
        return this;
    }
}
