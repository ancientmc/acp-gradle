package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.Json;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.util.Map;

/**
 * @author moist-mason
 */
public class ResolveTools extends Step {

    /**
     * The tool map. Retrieved from a function in the JSON utility class. They key is the tool's configuration, while the
     * value is the tool's maven path.
     * @see Json#getTools(File)
     */
    private Map<String, String> tools;

    public ResolveTools(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() {
        tools.forEach((configuration, tool) -> {
            Configuration cfg = project.getConfigurations().named(configuration).get();
            resolve(cfg, tool);
        });
    }

    public void resolve(Configuration cfg, String tool) {
        Dependency dependency = project.getDependencies().create(tool);

        if (!cfg.getDependencies().contains(dependency)) {
            logger.functions().resolve(tool);
            cfg.getDependencies().add(dependency);
        }
    }

    public ResolveTools setTools(Map<String, String> tools) {
        this.tools = tools;
        return this;
    }
}
