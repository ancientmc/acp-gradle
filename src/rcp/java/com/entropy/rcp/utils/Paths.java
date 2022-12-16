package com.entropy.rcp.utils;

public class Paths {
    private static final String OS_NAME = OSName.getOSName();

    public static final String MC_VERSION = "a1.2.6";
    public static final String MC_JAR = "https://piston-data.mojang.com/v1/objects/a68c817afd6c05c253ba5462287c2c19bbb57935/client.jar";
    public static final String MC_JSON = "https://piston-meta.mojang.com/v1/packages/b539c8b7baabb3ab8886fa634310de51aeea60fc/a1.2.6.json";

    public static final String RCP_DIR_CORE = "core\\";
    public static final String RCP_DIR_MAPPING = RCP_DIR_CORE + "mapping\\";
    public static final String RCP_DIR_LOGS = RCP_DIR_CORE + "logs\\";
    public static final String RCP_DIR_TEMP = RCP_DIR_CORE + "temp\\";
    public static final String RCP_DIR_PATCHES = RCP_DIR_CORE + "patches\\";
    public static final String RCP_PATCH_FILES = RCP_DIR_PATCHES + "patch\\";
    public static final String RCP_DIR_RUN = "run\\";
    public static final String RCP_DIR_NATIVES = RCP_DIR_RUN + "bin\\natives\\" + OS_NAME + "\\";
    public static final String RCP_DIR_SRC = "src\\main\\java\\";
    public static final String RCP_DIR_RESOURCES = "src\\main\\resources\\";
    public static final String RCP_ASSET_EXTRACTOR = RCP_DIR_PATCHES + "McAssetExtractor-1.0_03-1-all.jar";
    public static final String BASE_JAR = RCP_DIR_TEMP + MC_VERSION + ".jar";
    public static final String SRG_JAR = RCP_DIR_TEMP + MC_VERSION + "-srg.jar";
    public static final String EXC_JAR = RCP_DIR_TEMP + MC_VERSION + "-exc.jar";
    public static final String FINAL_JAR = RCP_DIR_TEMP + MC_VERSION + "-final.jar";
    public static final String JSON_FILE = RCP_DIR_TEMP + MC_VERSION + ".json";
    public static final String JAR_DEP_JSON = RCP_DIR_CORE + MC_VERSION + "-jardep.json";
    public static final String NATIVES_JSON = RCP_DIR_CORE + MC_VERSION + "-natives.json";

}
