package com.ancientmc.acp;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class AcpExtension {
    /**
     * The gradle project.
     */
    protected final Project project;

    /**
     * The data property, linked to the ZIP file containing mappings and patch files ACP needs to decompile Minecraft.
     */
    private final Property<String> data;

    /**
     * The mod loader. Acceptable options are 'risugami' (for Risugami's Mod Loader) and 'forge' (for Minecraft Forge).
     */
    private final Property<String> loader;

    /**
     * The directory where the generated diff patches for mods are located. Customizable by the user.
     */
    private final Property<String> diffPatchesDir;

    /**
     * The customizable name of the mod.
     */
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
