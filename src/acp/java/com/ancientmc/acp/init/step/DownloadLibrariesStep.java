package com.ancientmc.acp.init.step;

import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.logging.Logger;

import java.util.List;

public class DownloadLibrariesStep extends Step {
    private List<String> libraries;
    private Project project;

    @Override
    public void exec() {
        DependencySet dependencies = project.getConfigurations().getByName("implementation").getDependencies();

        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            public void beforeResolve(ResolvableDependencies resolvableDependencies) {
                libraries.forEach(lib -> dependencies.add(project.getDependencies().create(lib)));
                project.getGradle().removeListener(this);
            }

            @Override
            public void afterResolve(ResolvableDependencies resolvableDependencies) { }
        });
    }

    public DownloadLibrariesStep setLibraries(List<String> libraries) {
        this.libraries = libraries;
        return this;
    }

    public DownloadLibrariesStep setProject(Project project) {
        this.project = project;
        return this;
    }
}
