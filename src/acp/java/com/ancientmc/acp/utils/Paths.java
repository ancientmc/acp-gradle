package com.ancientmc.acp.utils;

public class Paths {
    private static final String OS_NAME = OSName.getOSName();

    public static final String MC_VERSION = "a1.2.6";
    public static final String MC_JAR = "https://piston-data.mojang.com/v1/objects/a68c817afd6c05c253ba5462287c2c19bbb57935/client.jar";
    public static final String MC_JSON = "https://piston-meta.mojang.com/v1/packages/b539c8b7baabb3ab8886fa634310de51aeea60fc/a1.2.6.json";

    public static final String DIR_CFG = "cfg\\";
    public static final String DIR_MAPPINGS = DIR_CFG + "mappings\\";
    public static final String DIR_LOGS = DIR_CFG + "logs\\";
    public static final String DIR_TEMP = DIR_CFG + "temp\\";
    public static final String DIR_PATCHES = DIR_CFG + "patches\\";
    public static final String DIR_RUN = "run\\";
    public static final String DIR_NATIVES = DIR_RUN + "bin\\natives\\";
    public static final String DIR_SRC = "src\\main\\java\\";
    public static final String DIR_RESOURCES = "src\\main\\resources\\";
    public static final String BASE_JAR = DIR_TEMP + MC_VERSION + ".jar";
    public static final String SRG_JAR = DIR_TEMP + MC_VERSION + "-srg.jar";
    public static final String EXC_JAR = DIR_TEMP + MC_VERSION + "-exc.jar";
    public static final String FINAL_JAR = DIR_TEMP + MC_VERSION + "-final.jar";
}
