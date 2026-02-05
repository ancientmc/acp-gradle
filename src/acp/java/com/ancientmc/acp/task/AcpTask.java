package com.ancientmc.acp.task;

import com.ancientmc.acp.logger.AcpLogger;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Base class for all ACP-related Gradle tasks.
 * @author moist-mason
 */
public abstract class AcpTask extends DefaultTask {

    /** ACP's custom logger. **/
    public AcpLogger logger;

    public AcpTask() {
        setGroup("acp"); // automatically sets the group for all ACP tasks.
    }


    /** Main task action. */
    @TaskAction
    public void exec() {
        Project project = getProject();
        String phase = getPhase().get();
        this.logger = new AcpLogger(phase, getProject());

        function(project);
        logger.write();
    }


    /**
     * Runs the main function of the task. Inherited by all children of the base ACP task class.
     * @param project The gradle project.
     */
    public abstract void function(Project project);

    @Input
    public abstract Property<String> getPhase();
}
