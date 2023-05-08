package com.ancientmc.modtools;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;

public class ModToolsExtension {
    protected final Project project;
    private final Property<String> loader;
    private final Property<String> diffPatchesDir;
    private final Property<String> modName;

    public ModToolsExtension(final Project project) {
        this.project = project;
        this.loader = project.getObjects().property(String.class);
        this.diffPatchesDir = project.getObjects().property(String.class);
        this.modName = project.getObjects().property(String.class);
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
