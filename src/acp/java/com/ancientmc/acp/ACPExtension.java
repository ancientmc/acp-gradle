package com.ancientmc.acp;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class ACPExtension {
    protected final Project project;
    private final Property<String> data;

    public ACPExtension(final Project project) {
        this.project = project;
        this.data = project.getObjects().property(String.class);
    }

    public Property<String> getData() {
        return this.data;
    }
}
