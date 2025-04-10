package com.ancientmc.acp.task;

import com.ancientmc.acp.logger.AcpLogger;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

/**
 * @author moist-mason
 */
public abstract class AcpTask extends DefaultTask {
    public AcpLogger logger;

    public AcpTask() {
        setGroup("acp");
    }

    @Input
    public abstract Property<String> getPhase();

    @Internal
    protected void setLogger() {
        String phase = getPhase().get();
        this.logger = new AcpLogger(phase, getProject());
    }
}
