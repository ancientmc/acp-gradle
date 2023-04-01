package com.ancientmc.acp;

import com.ancientmc.acp.init.ACPInitialization;
import com.ancientmc.acp.tasks.InjectModLoader;
import com.ancientmc.acp.utils.Paths;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.GradleBuild;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ACPPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        String minecraftVersion = project.getExtensions().getExtraProperties().get("MC_VERSION").toString();
        ACPExtension extension = project.getExtensions().create("acp", ACPExtension.class, project);

        project.getPluginManager().apply(JavaPlugin.class);

        Configuration jarsplitter = project.getConfigurations().getByName("jarsplitter");
        Configuration binpatcher = project.getConfigurations().getByName("binpatcher");
        Configuration mcinjector = project.getConfigurations().getByName("mcinjector");
        Configuration forgeart = project.getConfigurations().getByName("forgeart");
        Configuration fernflower = project.getConfigurations().getByName("fernflower");
        Configuration diffpatch = project.getConfigurations().getByName("diffpatch");

        TaskProvider<JavaExec> stripJar = project.getTasks().register("stripJar", JavaExec.class);
        TaskProvider<InjectModLoader> injectModloader = project.getTasks().register("injectModloader", InjectModLoader.class);
        TaskProvider<JavaExec> mcinject = project.getTasks().register("mcinject", JavaExec.class);
        TaskProvider<JavaExec> deobfJar = project.getTasks().register("deobfJar", JavaExec.class);
        TaskProvider<JavaExec> decompile = project.getTasks().register("decompile", JavaExec.class);
        TaskProvider<Copy> unzip = project.getTasks().register("unzip", Copy.class);
        TaskProvider<JavaExec> patch = project.getTasks().register("patch", JavaExec.class);
        TaskProvider<Copy> copyJarAssets = project.getTasks().register("copyJarAssets", Copy.class);
        TaskProvider<Copy> copySrc = project.getTasks().register("copySrc", Copy.class);

        TaskProvider<GradleBuild> execute = project.getTasks().register("execute", GradleBuild.class);

        project.afterEvaluate(proj -> {
            try {
                ACPInitialization.init(proj, extension, minecraftVersion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        stripJar.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Strips the JAR into two, one JAR containing the core Minecraft classes, and the other containing everything else.");
            task.getMainClass().set("net.minecraftforge.jarsplitter.ConsoleTool");
            task.setClasspath(project.files(jarsplitter));
            task.args("--input", Paths.BASE_JAR, "--slim", Paths.SLIM_JAR, "--extra", Paths.EXTRA_JAR, "--srg", Paths.SRG);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        // The modloader will be injected via a binpatch archive, in the LZMA format. The binpatch for Risugami's Modloader will
        // be uploaded to maven and downloadable through a task.

        File loaderPatches = new File(Paths.DIR_MODLOADER_PATCHES);

        injectModloader.configure(task -> {
            task.setGroup("decomp");
            task.dependsOn(stripJar);
            task.getInputJar().set(project.file(Paths.SLIM_JAR));
            task.getPatchDir().set(loaderPatches);
            task.getOutputJar().set(project.file(Paths.MODLOADER_JAR));
        });

        boolean vanilla = !loaderPatches.exists();
        String toInject = (vanilla ? Paths.SLIM_JAR : Paths.MODLOADER_JAR);
        String dependent = (vanilla ? "stripJar" : "injectModloader");

        mcinject.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Injects the slim JAR with local variables, exceptions, and other stuff to eliminate errors.");
            task.dependsOn(project.getTasks().getByName(dependent));
            task.getMainClass().set("de.oceanlabs.mcp.mcinjector.MCInjector");
            task.setClasspath(project.files(mcinjector));
            task.args("--in", toInject, "--out", Paths.INJECT_JAR, "--exc", Paths.DIR_MAPPINGS + "exceptions.txt");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        deobfJar.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Deobfuscates the JAR with human-readable names.");
            task.dependsOn(mcinject);
            task.getMainClass().set("net.minecraftforge.fart.Main");
            task.setClasspath(project.files(forgeart));
            task.args("--input", Paths.INJECT_JAR, "--output", Paths.SRG_JAR, "--map", Paths.SRG);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        decompile.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Decompiles the JAR.");
            task.dependsOn(deobfJar);
            task.getMainClass().set("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler");
            task.setClasspath(project.files(fernflower));
            task.args("-rbr=0", "-rsy=0", "-asc=1", "-dgs=0", "-jvn=1", Paths.SRG_JAR, Paths.FINAL_JAR);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        unzip.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Unzips the java source files into the src directory.");
            task.dependsOn(decompile);
            task.from(project.zipTree(project.file(Paths.FINAL_JAR)));
            task.into(project.file(Paths.DIR_SRC));
        });

        patch.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Patches the source files to make the game able to compile.");
            task.dependsOn(unzip);
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(diffpatch));
            task.args("--patch", Paths.DIR_SRC, Paths.DIR_PATCHES, "--output", Paths.DIR_SRC,
                    "--reject", Paths.DIR_TEMP + "patch_rejects\\");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        copyJarAssets.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Copies the JAR assets into the src/main/resources folder.");
            task.dependsOn(patch);
            task.from(project.zipTree(project.file(Paths.EXTRA_JAR)));
            task.into(project.file(Paths.DIR_RESOURCES));
            task.exclude("com/**", "paulscode/**");
        });

        copySrc.configure(task -> {
           task.setGroup("decomp");
           task.dependsOn(copyJarAssets);
           task.from(project.zipTree(project.file(Paths.FINAL_JAR)));
           task.into(project.file(project.getBuildDir().getAbsolutePath() + "\\modding\\backupSrc\\"));
        });
    }
}
