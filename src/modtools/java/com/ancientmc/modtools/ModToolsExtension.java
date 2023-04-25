package com.ancientmc.modtools;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class ModToolsExtension {
    protected final Project project;
    private final Property<String> loader;
    private final Property<String> binPatchesDir;
    private final Property<String> diffPatchesDir;

    public ModToolsExtension(final Project project) {
        this.project = project;
        this.loader = project.getObjects().property(String.class);
        this.binPatchesDir = project.getObjects().property(String.class);
        this.diffPatchesDir = project.getObjects().property(String.class);
    }

    public Property<String> getLoader() {
        return this.loader;
    }

    public Property<String> getBinPatchesDir() {
        return binPatchesDir;
    }

    public Property<String> getDiffPatchesDir() {
        return diffPatchesDir;
    }
}
