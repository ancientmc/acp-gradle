package com.ancientmc.acp;

import com.ancientmc.acp.init.InitDownloadLib;
import com.ancientmc.acp.tasks.*;
import com.ancientmc.acp.utils.Paths;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ACPPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        final String minecraftVersion = project.getExtensions().getExtraProperties().get("MC_VERSION").toString();
        final String minecraftRepo = project.getExtensions().getExtraProperties().get("MC_REPO").toString();

        project.getPluginManager().apply(JavaPlugin.class);

        Configuration retroguard = project.getConfigurations().getByName("retroguard");
        Configuration mcinjector = project.getConfigurations().getByName("mcinjector");
        Configuration enigma = project.getConfigurations().getByName("enigma");
        Configuration quiltflower = project.getConfigurations().getByName("quiltflower");
        Configuration diffpatch = project.getConfigurations().getByName("diffpatch");

        TaskProvider<DownloadJar> downloadJar = project.getTasks().register("downloadJar", DownloadJar.class);
        TaskProvider<JavaExec> deobfJar = project.getTasks().register("deobfJar", JavaExec.class);
        TaskProvider<JavaExec> injectExceptions = project.getTasks().register("injectExceptions", JavaExec.class);
        TaskProvider<JavaExec> addParams = project.getTasks().register("addParams", JavaExec.class);
        TaskProvider<JavaExec> decompileClassFiles = project.getTasks().register("decompileClassFiles", JavaExec.class);
        TaskProvider<Copy> unzipJar = project.getTasks().register("unzipJar", Copy.class);
        TaskProvider<JavaExec> patchSourceFiles = project.getTasks().register("patchSourceFiles", JavaExec.class);
        TaskProvider<Copy> copyJarAssets = project.getTasks().register("copyJarAssets", Copy.class);
        TaskProvider<JavaExec> downloadMetaAssets = project.getTasks().register("downloadMetaAssets", JavaExec.class);
        TaskProvider<DownloadNatives> downloadNatives = project.getTasks().register("downloadNatives", DownloadNatives.class);
        TaskProvider<ExtractNatives> extractNatives = project.getTasks().register("extractNatives", ExtractNatives.class);

        /**
         * Internal
         */
        TaskProvider<GradleBuild> execute = project.getTasks().register("execute", GradleBuild.class);

        TaskProvider<JavaExec> reobfJar = project.getTasks().register("reobfJar", JavaExec.class);

        project.afterEvaluate(p -> {
            File json = p.file(Paths.JSON_FILE);
            if(!json.exists()) {
                try {
                    FileUtils.copyURLToFile(new URL(Paths.MC_JSON), json);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if(!new File(minecraftRepo).exists()) {
                try {
                    InitDownloadLib.init(json, new File(minecraftRepo));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    InitDownloadLib.init(p.file(Paths.JAR_DEP_JSON), new File(minecraftRepo));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        downloadJar.configure(task -> {
            try {
                task.setGroup("acp-decomp");
                task.getURL().set(new URL(Paths.MC_JAR));
                task.getOutput().set(new File(Paths.BASE_JAR));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
        deobfJar.configure(task -> {
            task.setGroup("acp-decomp");
            task.getMainClass().set("RetroGuard");
            task.setClasspath(project.files(retroguard));
            task.args("-searge", Paths.ACP_DIR_MAPPING + "retroguard.cfg");
        });
        injectExceptions.configure(task -> {
            task.setGroup("acp-decomp");
            task.getMainClass().set("de.oceanlabs.mcp.mcinjector.MCInjector");
            task.setClasspath(project.files(mcinjector));
            task.args("--in", Paths.SRG_JAR, "--out", Paths.EXC_JAR, "--exc", Paths.ACP_DIR_MAPPING + "exceptions.exc", "--log", Paths.ACP_DIR_LOGS + "exceptions.log");
        });
        addParams.configure(task -> {
            task.setGroup("acp-decomp");
            task.getMainClass().set("cuchaz.enigma.command.Main");
            task.setClasspath(project.files(enigma));
            task.args("deobfuscate", Paths.EXC_JAR, Paths.FINAL_JAR, Paths.ACP_DIR_MAPPING + "params.mapping");

            // Enigma needs Java 17 to run. This is here so we don't have to set the JDK version in the end-user gradle.
            JavaToolchainService javaToolchainService = task.getProject().getExtensions().getByType(JavaToolchainService.class);
            task.getJavaLauncher().set(javaToolchainService.launcherFor(javaToolchainSpec ->
                    javaToolchainSpec.getLanguageVersion().set(JavaLanguageVersion.of(17))));
        });
        decompileClassFiles.configure(task -> {
            task.setGroup("acp-decomp");
            task.getMainClass().set("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler");
            task.setClasspath(project.files(quiltflower));
            task.args("-rbr=0", "-rsy=0", "-asc=1", "-dgs=1", "-jvn=1", "-dec=0", Paths.FINAL_JAR, Paths.FINAL_JAR);
        });
        unzipJar.configure(task -> {
            task.setGroup("acp-decomp");
            task.from(project.zipTree(project.file(Paths.FINAL_JAR)));
            task.into(project.file(Paths.ACP_DIR_SRC));
            task.exclude("com/**", "paulscode/**");
        });
        patchSourceFiles.configure(task -> {
            task.setGroup("acp-decomp");
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(diffpatch));
            task.args("--patch", Paths.ACP_DIR_SRC, Paths.ACP_PATCH_FILES, "--output", Paths.ACP_DIR_SRC,
                    "--reject", Paths.ACP_DIR_LOGS + "patch_rejects\\", "--verbose");
        });
        copyJarAssets.configure(task -> {
            task.setGroup("acp-decomp");
            task.dependsOn(downloadJar);
            task.from(project.zipTree(project.file(Paths.BASE_JAR)));
            task.into(project.file(Paths.ACP_DIR_RESOURCES));
            task.exclude("com/**", "net/**", "paulscode/**", "*.class");
        });
        downloadMetaAssets.configure(task -> {
            task.setGroup("acp-decomp");
            task.getMainClass().set("com.github.rmheuer.mcasset.McAssetExtractor");
            task.setClasspath(project.files(Paths.ACP_ASSET_EXTRACTOR));
            task.args(minecraftVersion, project.file(Paths.ACP_DIR_RUN));
        });
        downloadNatives.configure(task -> {
            task.setGroup("acp-decomp");
            task.getJson().set(new File(Paths.NATIVES_JSON));
            task.getNativesDir().set(new File(Paths.ACP_DIR_NATIVES));
        });
        extractNatives.configure(task -> {
            task.setGroup("acp-decomp");
            task.getNativesDir().set(new File(Paths.ACP_DIR_NATIVES));
        });

        /** Internal **/
        execute.configure(task -> {
            List<String> taskList = new ArrayList<>();

            taskList.add(project.getTasks().getByName("downloadJar").getName());
            taskList.add(project.getTasks().getByName("deobfJar").getName());
            taskList.add(project.getTasks().getByName("injectExceptions").getName());
            taskList.add(project.getTasks().getByName("addParams").getName());
            taskList.add(project.getTasks().getByName("decompileClassFiles").getName());
            taskList.add(project.getTasks().getByName("unzipJar").getName());
            taskList.add(project.getTasks().getByName("patchSourceFiles").getName());
            taskList.add(project.getTasks().getByName("copyJarAssets").getName());
            taskList.add(project.getTasks().getByName("downloadMetaAssets").getName());
            taskList.add(project.getTasks().getByName("downloadNatives").getName());
            taskList.add(project.getTasks().getByName("extractNatives").getName());

            task.setTasks(taskList);
        });

        reobfJar.configure(task -> {
            task.setGroup("acp-reobf");
            task.dependsOn(project.getTasks().getByName("jar"));
            task.getMainClass().set("RetroGuard");
            task.setClasspath(project.files(retroguard));
            task.args("-notch", Paths.ACP_DIR_MAPPING + "retroguard.cfg");
            task.doLast(c -> c.getProject().delete("build\\libs\\interm-client-a1.2.6.jar"));
        });
    }
}
