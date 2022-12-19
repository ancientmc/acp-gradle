package com.ancientmc.acp.utils;

public class Paths {
    private static final String OS_NAME = OSName.getOSName();

    public static final String MC_VERSION = "a1.2.6";
    public static final String MC_JAR = "https://piston-data.mojang.com/v1/objects/a68c817afd6c05c253ba5462287c2c19bbb57935/client.jar";
    public static final String MC_JSON = "https://piston-meta.mojang.com/v1/packages/b539c8b7baabb3ab8886fa634310de51aeea60fc/a1.2.6.json";

    public static final String ACP_DIR_CORE = "core\\";
    public static final String ACP_DIR_MAPPING = ACP_DIR_CORE + "mapping\\";
    public static final String ACP_DIR_LOGS = ACP_DIR_CORE + "logs\\";
    public static final String ACP_DIR_TEMP = ACP_DIR_CORE + "temp\\";
    public static final String ACP_DIR_PATCHES = ACP_DIR_CORE + "patches\\";
    public static final String ACP_PATCH_FILES = ACP_DIR_PATCHES + "patch\\";
    public static final String ACP_DIR_RUN = "run\\";
    public static final String ACP_DIR_NATIVES = ACP_DIR_RUN + "bin\\natives\\" + OS_NAME + "\\";
    public static final String ACP_DIR_SRC = "src\\main\\java\\";
    public static final String ACP_DIR_RESOURCES = "src\\main\\resources\\";
    public static final String ACP_ASSET_EXTRACTOR = ACP_DIR_PATCHES + "McAssetExtractor-1.0_03-1-all.jar";
    public static final String BASE_JAR = ACP_DIR_TEMP + MC_VERSION + ".jar";
    public static final String SRG_JAR = ACP_DIR_TEMP + MC_VERSION + "-srg.jar";
    public static final String EXC_JAR = ACP_DIR_TEMP + MC_VERSION + "-exc.jar";
    public static final String FINAL_JAR = ACP_DIR_TEMP + MC_VERSION + "-final.jar";
    public static final String JSON_FILE = ACP_DIR_TEMP + MC_VERSION + ".json";
    public static final String JAR_DEP_JSON = ACP_DIR_CORE + MC_VERSION + "-jardep.json";
    public static final String NATIVES_JSON = ACP_DIR_CORE + MC_VERSION + "-natives.json";

}
