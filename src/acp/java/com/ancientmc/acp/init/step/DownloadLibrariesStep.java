package com.ancientmc.acp.init.step;

import com.ancientmc.acp.utils.Json;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.logging.Logger;

import java.util.List;

/**
 * Downloads Minecraft's libraries as Gradle dependencies.
 */
public class DownloadLibrariesStep extends Step {
    /**
     * The list of Minecraft's libraries, formatted via maven path (group.sub:name:version)
     */
    private List<String> libraries;
    /**
     * The Gradle project.
     */
    private Project project;

    /**
     * A list of all of Minecraft's libraries get parsed through and resolved via a Gradle listener.
     * The list of libraries obtained via a method in the Json utilities class.
     * @see Json#getLibraries(List)
     */
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
