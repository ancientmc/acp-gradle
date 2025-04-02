package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.Json;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;

import java.util.List;

/**
 * Downloads Minecraft's libraries as Gradle dependencies.
 */
public class ResolveLibraries extends Step {

    /**
     * The list of Minecraft's libraries, formatted via maven path (group.sub:name:version)
     */
    private List<String> libraries;

    /**
     * A list of all of Minecraft's libraries get parsed through and resolved via a Gradle listener.
     * The list of libraries obtained via a method in the Json utilities class.
     * @see Json#getLibraries(List)
     */

    public ResolveLibraries(Project project, String phase, String message) {
        build(project, phase, message);
    }

    @Override
    public void action() {
        DependencySet dependencies = project.getConfigurations().named("implementation").get().getDependencies();

        libraries.forEach(lib -> {
            Dependency dependency = project.getDependencies().create(lib);
            if (!dependencies.contains(dependency)) {
                project.getLogger().info("Resolve {}", lib);
                dependencies.add(dependency);
            }
        });
    }

    public ResolveLibraries setLibraries(List<String> libraries) {
        this.libraries = libraries;
        return this;
    }
}
