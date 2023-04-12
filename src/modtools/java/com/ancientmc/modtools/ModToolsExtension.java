package com.ancientmc.modtools;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class ModToolsExtension {
    protected final Project project;
    private final Property<String> type;
    private final Property<String> binPatchesDir;
    private final Property<String> diffPatchesDir;

    public ModToolsExtension(final Project project) {
        this.project = project;
        this.type = project.getObjects().property(String.class);
        this.binPatchesDir = project.getObjects().property(String.class);
        this.diffPatchesDir = project.getObjects().property(String.class);
    }

    public Property<String> getType() {
        return this.type;
    }

    public Property<String> getBinPatchesDir() {
        return binPatchesDir;
    }

    public Property<String> getDiffPatchesDir() {
        return diffPatchesDir;
    }
}
