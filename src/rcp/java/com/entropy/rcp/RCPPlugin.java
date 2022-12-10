package com.entropy.rcp;

import com.entropy.rcp.tasks.*;
import com.entropy.rcp.utils.Paths;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class RCPPlugin implements Plugin<Project> {

    public static final String MC_VERSION = "a1.2.6";

    @Override
    public void apply(Project project) {
        final String minecraftRepo = project.getGradle().getGradleUserHomeDir().getAbsolutePath() + "/caches/retrocoderpack/mc_repo/";

        project.getPluginManager().apply(JavaPlugin.class);

        Configuration retroguard = project.getConfigurations().getByName("retroguard");
        Configuration mcinjector = project.getConfigurations().getByName("mcinjector");
        Configuration enigma = project.getConfigurations().getByName("enigma");
        Configuration quiltflower = project.getConfigurations().getByName("quiltflower");
        Configuration diffpatch = project.getConfigurations().getByName("diffpatch");

        TaskProvider<DownloadJar> downloadJar = project.getTasks().register("downloadJar", DownloadJar.class);
        TaskProvider<DownloadJson> downloadJson = project.getTasks().register("downloadJson", DownloadJson.class);
        TaskProvider<JavaExec> deobfJar = project.getTasks().register("deobfJar", JavaExec.class);
        TaskProvider<JavaExec> injectExceptions = project.getTasks().register("injectExceptions", JavaExec.class);
        TaskProvider<JavaExec> addParams = project.getTasks().register("addParams", JavaExec.class);
        TaskProvider<JavaExec> decompileClassFiles = project.getTasks().register("decompileClassFiles", JavaExec.class);
        TaskProvider<Copy> unzipJar = project.getTasks().register("unzipJar", Copy.class);
        TaskProvider<JavaExec> patchSourceFiles = project.getTasks().register("patchSourceFiles", JavaExec.class);
        TaskProvider<Copy> copyJarAssets = project.getTasks().register("copyJarAssets", Copy.class);
        TaskProvider<JavaExec> downloadMetaAssets = project.getTasks().register("downloadMetaAssets", JavaExec.class);
        TaskProvider<DownloadLibraries> downloadLibraries = project.getTasks().register("downloadLibraries", DownloadLibraries.class);
        TaskProvider<DownloadLibraries> downloadJarLibraries = project.getTasks().register("downloadJarLibraries", DownloadLibraries.class);
        TaskProvider<DownloadNatives> downloadNatives = project.getTasks().register("downloadNatives", DownloadNatives.class);
        TaskProvider<ExtractNatives> extractNatives = project.getTasks().register("extractNatives", ExtractNatives.class);
        TaskProvider<CleanTasks.CleanTemp> cleanTemp = project.getTasks().register("cleanTemp", CleanTasks.CleanTemp.class);
        TaskProvider<CleanTasks.CleanAll> cleanAll = project.getTasks().register("cleanAll", CleanTasks.CleanAll.class);

        downloadJar.configure(task -> {
            try {
                task.setGroup("rcp");
                task.getURL().set(new URL(Paths.MC_JAR));
                task.getOutput().set(new File(Paths.BASE_JAR));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
        downloadJson.configure(task -> {
            try {
                task.setGroup("rcp");
                task.getURL().set(new URL(Paths.MC_JSON));
                task.getOutput().set(new File(Paths.JSON_FILE));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
        deobfJar.configure(task -> {
            task.setGroup("rcp");
            task.getMainClass().set("RetroGuard");
            task.setClasspath(project.files(retroguard));
            task.args("-searge", Paths.RCP_DIR_MAPPING + "retroguard.cfg");
        });
        injectExceptions.configure(task -> {
            task.setGroup("rcp");
            task.getMainClass().set("de.oceanlabs.mcp.mcinjector.MCInjector");
            task.setClasspath(project.files(mcinjector));
            task.args("--in", Paths.SRG_JAR, "--out", Paths.EXC_JAR, "--exc", Paths.RCP_DIR_MAPPING + "exceptions.exc", "--log", Paths.RCP_DIR_LOGS + "exceptions.log");
        });
        addParams.configure(task -> {
            task.setGroup("rcp");
            task.getMainClass().set("cuchaz.enigma.command.Main");
            task.setClasspath(project.files(enigma));
            task.args("deobfuscate", Paths.EXC_JAR, Paths.FINAL_JAR, Paths.RCP_DIR_MAPPING + "params\\");
        });
        decompileClassFiles.configure(task -> {
            task.setGroup("rcp");
            task.getMainClass().set("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler");
            task.setClasspath(project.files(quiltflower));
            task.args("-rbr=0", "-rsy=0", "-asc=1", "-dgs=1", "-jvn=1", "-dec=0", Paths.FINAL_JAR, Paths.FINAL_JAR);
        });
        unzipJar.configure(task -> {
            task.setGroup("rcp");
            task.from(project.zipTree(project.file(Paths.FINAL_JAR)));
            task.into(project.file(Paths.RCP_DIR_SRC));
            task.exclude("com/**", "paulscode/**");
        });
        patchSourceFiles.configure(task -> {
            task.setGroup("rcp");
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(diffpatch));
            task.args("--patch", Paths.RCP_DIR_SRC, Paths.RCP_PATCH_FILES, "--output", Paths.RCP_DIR_SRC,
                    "--reject", Paths.RCP_DIR_LOGS + "patch_rejects\\", "--verbose");
        });
        copyJarAssets.configure(task -> {
            task.setGroup("rcp");
            task.from(project.zipTree(project.file(Paths.BASE_JAR)));
            task.into(project.file(Paths.RCP_DIR_RESOURCES));
            task.exclude("com/**", "net/**", "paulscode/**", "*.class");
        });
        downloadMetaAssets.configure(task -> {
            task.setGroup("rcp");
            task.getMainClass().set("com.github.rmheuer.mcasset.McAssetExtractor");
            task.setClasspath(project.files(Paths.RCP_ASSET_EXTRACTOR));
            task.args(MC_VERSION, project.file(Paths.RCP_DIR_RUN));
        });
        downloadLibraries.configure(task -> {
            task.setGroup("rcp");
            task.getJson().set(new File(Paths.JSON_FILE));
            task.getRepository().set(new File(minecraftRepo));
        });
        downloadJarLibraries.configure(task -> {
            task.setGroup("rcp");
            task.getJson().set(new File(Paths.JAR_DEP_JSON));
            task.getRepository().set(new File(minecraftRepo));
        });
        downloadNatives.configure(task -> {
            task.setGroup("rcp");
            task.getJson().set(new File(Paths.NATIVES_JSON));
            task.getNativesDir().set(new File(Paths.RCP_DIR_NATIVES));
        });
        extractNatives.configure(task -> {
            task.setGroup("rcp");
            task.getNativesDir().set(new File(Paths.RCP_DIR_NATIVES));
        });
        cleanTemp.configure(task -> {
            task.setGroup("rcp");
        });
        cleanAll.configure(task -> {
            task.setGroup("rcp");
        });
    }
}
