package com.ancientmc.acp;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

/**
 * Plugin extension that lets end-users control how ACP decompiles and builds mods.
 * @author moist-mason
 */
public class AcpExtension {

    /** The Gradle project. */
    protected final Project project;

    /** The data property, linked to the ZIP file containing mappings and patch files ACP needs to decompile Minecraft. */
    private final Property<String> data;

    /** The mod loader. Acceptable options are 'risugami' (for Risugami's Mod Loader) and 'forge' (for Minecraft Forge). */
    private final Property<String> loader;

    /** The customizable name of the mod. Affects the name of the archives built by the BuildMod task. */
    private final Property<String> modName;

    public AcpExtension(final Project project) {
        this.project = project;
        this.data = project.getObjects().property(String.class);
        this.loader = project.getObjects().property(String.class);
        this.modName = project.getObjects().property(String.class);
    }

    public Property<String> getData() {
        return this.data;
    }

    public Property<String> getLoader() {
        return loader;
    }

    public Property<String> getModName() {
        return modName;
    }
}
