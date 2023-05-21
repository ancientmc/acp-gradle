package com.ancientmc.modtools;

import com.ancientmc.acp.tasks.GenerateHashes;
import com.ancientmc.acp.utils.Paths;
import com.ancientmc.modtools.tasks.DownloadModLoader;
import com.ancientmc.modtools.tasks.MakeReobfSrg;
import com.ancientmc.modtools.tasks.MakeZip;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;

public class ModToolsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        String minecraftVersion = project.getExtensions().getExtraProperties().get("MC_VERSION").toString();
        ModToolsExtension extension = project.getExtensions().create("modtools", ModToolsExtension.class, project);

        project.getPluginManager().apply(JavaPlugin.class);

        TaskProvider<DownloadModLoader> downloadModLoader = project.getTasks().register("downloadModLoader", DownloadModLoader.class);
        TaskProvider<JavaExec> genDiffPatches = project.getTasks().register("genDiffPatches", JavaExec.class);
        TaskProvider<GenerateHashes> generateModdedHashes = project.getTasks().register("generateModdedHashes", GenerateHashes.class);
        TaskProvider<MakeReobfSrg> makeReobfSrg = project.getTasks().register("makeReobfSrg", MakeReobfSrg.class);
        TaskProvider<JavaExec> reobfJar = project.getTasks().register("reobfJar", JavaExec.class);
        TaskProvider<Copy> extractReobfClasses = project.getTasks().register("extractReobfClasses", Copy.class);
        TaskProvider<MakeZip> makeZip = project.getTasks().register("makeZip", MakeZip.class);

        Configuration diffpatch = project.getConfigurations().getByName("diffpatch");
        Configuration specialsource = project.getConfigurations().getByName("specialsource");

        project.afterEvaluate(proj -> {
            String diffPatches = extension.getDiffPatchesDir().get();
            try {
                if (!proj.file(diffPatches).exists()) {
                    FileUtils.forceMkdir(proj.file(diffPatches));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        downloadModLoader.configure(task -> {
            String loaderType = extension.getLoader().get();

            task.setGroup("modtools");
            task.getVersion().set(minecraftVersion);
            task.getOutputDir().set(project.file(Paths.DIR_MODLOADER_PATCHES));
            task.getModLoader().set(loaderType);
        });

        genDiffPatches.configure(task -> {
            String diffPatches = extension.getDiffPatchesDir().get();
            task.setGroup("modtools");
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(diffpatch));
            task.args("--diff", Paths.DIR_ORIGINAL_SRC, Paths.DIR_SRC, "--output", diffPatches);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
            task.setIgnoreExitValue(true);
        });

        makeReobfSrg.configure(task -> {
            task.setGroup("modtools");
            task.getInputSrg().set(project.file(Paths.SRG));
            task.getOutputSrg().set(project.file(Paths.REOBF_SRG));
        });

        reobfJar.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(":jar", makeReobfSrg);
            task.getMainClass().set("net.md_5.specialsource.SpecialSource");
            task.setClasspath(project.files(specialsource));
            task.args("--in-jar", Paths.INTERM_JAR, "--out-jar", Paths.REOBF_JAR, "--srg-in", Paths.REOBF_SRG, "--reverse");
            task.getLogging().captureStandardError(LogLevel.DEBUG);
        });

        extractReobfClasses.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(reobfJar);
            task.from(project.zipTree(project.file(Paths.REOBF_JAR))).include("*.class", "net\\");
            task.into(Paths.DIR_REOBF_CLASSES);
        });

        generateModdedHashes.configure(task -> {
            task.setGroup("modtools");
            task.dependsOn(extractReobfClasses);
            task.getClassesDirectory().set(project.file(Paths.DIR_MODDED_CLASSES));
            task.getOutput().set(new File("build\\modding\\hashes\\modded.md5"));
        });

        makeZip.configure(task -> {
            String name = extension.getModName().get();
            task.setGroup("modtools");
            task.dependsOn(generateModdedHashes);
            task.getObfuscatedClassDirectory().set(project.file(Paths.DIR_REOBF_CLASSES));
            task.getOriginalHash().set(project.file("build\\modding\\hashes\\vanilla.md5"));
            task.getModdedHash().set(project.file("build\\modding\\hashes\\modded.md5"));
            task.getSrg().set(project.file(Paths.SRG));
            task.getZip().set(project.file("build\\modding\\zip\\" + name + ".zip"));
        });
    }
}
