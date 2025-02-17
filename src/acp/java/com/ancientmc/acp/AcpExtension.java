package com.ancientmc.acp;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class AcpExtension {
    /**
     * The gradle project.
     */
    protected final Project project;

    /**
     * The data property.
     */
    private final Property<String> data;

    private final Property<String> loader;
    private final Property<String> diffPatchesDir;
    private final Property<String> modName;

    public AcpExtension(final Project project) {
        this.project = project;
        this.data = project.getObjects().property(String.class);
        this.loader = project.getObjects().property(String.class);
        this.diffPatchesDir = project.getObjects().property(String.class);
        this.modName = project.getObjects().property(String.class);
    }

    public Property<String> getData() {
        return this.data;
    }

    public Property<String> getLoader() {
        return loader;
    }

    public Property<String> getDiffPatchesDir() {
        return diffPatchesDir;
    }

    public Property<String> getModName() {
        return modName;
    }
}
