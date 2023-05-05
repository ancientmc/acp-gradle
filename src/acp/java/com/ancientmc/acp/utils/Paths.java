package com.ancientmc.acp.utils;

public class Paths {
    public static String DIR_CFG;
    public static String DIR_MAPPINGS;
    public static String DIR_LOGS;
    public static String DIR_TEMP;
    public static String DIR_PATCHES;
    public static String DIR_MODLOADER_PATCHES;
    public static String DIR_RUN;
    public static String DIR_NATIVES;
    public static String DIR_SRC;
    public static String DIR_ORIGINAL_SRC;
    public static String DIR_RESOURCES;
    public static String DIR_ORIGINAL_CLASSES;
    public static String DIR_REOBF_CLASSES;
    public static String DIR_MODDED_CLASSES;
    public static String BASE_JAR;
    public static String SLIM_JAR;
    public static String EXTRA_JAR;
    public static String MODLOADER_JAR;
    public static String SRG_JAR;
    public static String INJECT_JAR;
    public static String FINAL_JAR;
    public static String INTERM_JAR;
    public static String REOBF_JAR;
    public static String SRG;
    public static String REOBF_SRG;
    
    public static void init(String version) {
        DIR_CFG = "cfg\\";
        DIR_MAPPINGS = DIR_CFG + "mappings\\";
        DIR_LOGS = DIR_CFG + "logs\\";
        DIR_TEMP = DIR_CFG + "temp\\";
        DIR_PATCHES = DIR_CFG + "patches\\";
        DIR_MODLOADER_PATCHES = DIR_CFG + "modloader\\";
        DIR_RUN = "run\\";
        DIR_NATIVES = DIR_RUN + "bin\\natives\\";
        DIR_SRC = "src\\main\\java\\";
        DIR_ORIGINAL_SRC = "build\\modding\\originalSrc";
        DIR_RESOURCES = "src\\main\\resources\\";
        DIR_ORIGINAL_CLASSES = "build\\modding\\classes\\original\\";
        DIR_MODDED_CLASSES = "build\\classes\\java\\main\\";
        DIR_REOBF_CLASSES = "build\\modding\\classes\\reobf\\";
        BASE_JAR = DIR_TEMP + version + ".jar";
        SLIM_JAR = DIR_TEMP + version + "-slim.jar";
        EXTRA_JAR = DIR_TEMP + version + "-extra.jar";
        MODLOADER_JAR = DIR_TEMP + version + "-mod.jar";
        SRG_JAR = DIR_TEMP + version + "-srg.jar";
        INJECT_JAR = DIR_TEMP + version + "-inj.jar";
        FINAL_JAR = DIR_TEMP + version + "-final.jar";
        INTERM_JAR = "build\\libs\\interm-" + version + ".jar";
        REOBF_JAR = "build\\libs\\minecraft-" + version + ".jar";
        SRG = DIR_MAPPINGS + version + ".tsrg";
        REOBF_SRG = "build\\modding\\reobf.srg";
    }
}
