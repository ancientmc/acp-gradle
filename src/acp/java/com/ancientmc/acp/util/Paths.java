package com.ancientmc.acp.util;

/**
 * File containing all paths used by ACP.
 * @author moist-mason
 */
public final class Paths {

    /** Directories */
    public static final String DIR_CFG = "cfg/";
    public static final String DIR_INJECT = DIR_CFG + "inject/";
    public static final String DIR_TEMP = DIR_CFG + "temp/";
    public static final String DIR_PATCHES = DIR_CFG + "patches/";
    public static final String DIR_MODPATCHES = DIR_CFG + "modpatches/";
    public static final String DIR_START = DIR_CFG + "start/acp/client/";
    public static final String DIR_RUN = "run/";
    public static final String DIR_NATIVES = DIR_RUN + "bin/natives/";
    public static final String DIR_ASSETS = DIR_RUN + "resources/";
    public static final String DIR_SRC = "src/main/java/";
    public static final String DIR_RESOURCES = "src/main/resources";
    public static final String DIR_LOG = "log/";
    public static final String DIR_BUILD_VANILLA = "build/acp/vanilla/";
    public static final String DIR_BUILD_MODDED = "build/acp/modded/";
    public static final String DIR_VANILLA_SRC = DIR_BUILD_VANILLA + "src/";
    public static final String DIR_VANILLA_CLASSES = DIR_BUILD_VANILLA + "classes/";
    public static final String DIR_VANILLA_RESOURCES = DIR_BUILD_VANILLA + "resources/";
    public static final String DIR_REOBF_CLASSES = DIR_BUILD_MODDED + "classes/reobf/";
    public static final String DIR_MODDED_CLASSES = DIR_BUILD_MODDED + "classes/deobf/";
    public static final String DIR_MODDED_PATCHES = DIR_BUILD_MODDED + "patches/";

    /** ACP files. */
    public static final String ACP_DATA = DIR_CFG + "data.zip";
    public static final String TOOLS_JSON = "gradle/tools.json";
    public static final String TSRG = DIR_CFG + "cuneiform.tsrg";
    public static final String REOBF_SRG = DIR_BUILD_MODDED + "reobf.srg";
    public static final String VANILLA_HASH_FILE = DIR_BUILD_MODDED + "hashes/vanilla.md5";
    public static final String MODDED_HASH_FILE = DIR_BUILD_MODDED + "hashes/modded.md5";

    /** Minecraft files. */
    public static final String VERSION_MANIFEST = DIR_TEMP + "version_manifest.json";
    public static final String JSON = DIR_TEMP + "version.json";
    public static final String CLIENT_JAR = DIR_TEMP + "client.jar";
    public static final String SLIM_JAR = DIR_TEMP + "slim.jar";
    public static final String EXTRA_JAR = DIR_TEMP + "extra.jar";
    public static final String MODLOADER_JAR = DIR_TEMP + "mod.jar";
    public static final String MAPPED_JAR = DIR_TEMP + "mapped.jar";
    public static final String REPACKAGED_JAR = DIR_TEMP + "repackaged.jar";
    public static final String INJECT_JAR = DIR_TEMP + "inject.jar";
    public static final String FINAL_JAR = DIR_TEMP + "final.jar";
    public static final String VANILLA_JAR = DIR_BUILD_VANILLA + "vanilla.jar";
    public static final String INTERM_JAR = DIR_BUILD_MODDED + "jars/intermediate.jar";
    public static final String REOBF_JAR = DIR_BUILD_MODDED + "jars/reobf.jar";
}
