package com.ancientmc.acp;

import com.ancientmc.acp.init.ACPInitialization;
import com.ancientmc.acp.utils.Paths;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.GradleBuild;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;

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
        Configuration mcinjector = project.getConfigurations().getByName("mcinjector");
        Configuration forgeart = project.getConfigurations().getByName("forgeart");
        Configuration fernflower = project.getConfigurations().getByName("fernflower");
        Configuration diffpatch = project.getConfigurations().getByName("diffpatch");

        TaskProvider<JavaExec> stripJar = project.getTasks().register("stripJar", JavaExec.class);
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
        });

        // Mod loader class injection/binpatching will be sandwiched in here. Not worrying about it right now.

        mcinject.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Injects the slim JAR with local variables, exceptions, and other stuff to eliminate errors.");
            task.getMainClass().set("de.oceanlabs.mcp.mcinjector.MCInjector");
            task.setClasspath(project.files(mcinjector));
            task.args("--in", Paths.SLIM_JAR, "--out", Paths.INJECT_JAR, "--exc", Paths.DIR_MAPPINGS + "exceptions.txt", "--log", Paths.DIR_LOGS + "mcinjector.log");
        });

        deobfJar.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Deobfuscates the JAR with human-readable names.");
            task.getMainClass().set("net.minecraftforge.fart.Main");
            task.setClasspath(project.files(forgeart));
            task.args("--input", Paths.INJECT_JAR, "--output", Paths.SRG_JAR, "--map", Paths.SRG);
        });

        decompile.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Decompiles the JAR.");
            task.getMainClass().set("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler");
            task.setClasspath(project.files(fernflower));
            task.args("-rbr=0", "-rsy=0", "-asc=1", "-dgs=1", "-jvn=1", Paths.SRG_JAR, Paths.FINAL_JAR);
        });

        unzip.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Unzips the java source files into the src directory.");
            task.from(project.zipTree(project.file(Paths.FINAL_JAR)));
            task.into(project.file(Paths.DIR_SRC));
        });

        patch.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Patches the source files to make the game able to compile.");
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(diffpatch));
            task.args("--patch", Paths.DIR_SRC, Paths.DIR_PATCHES, "--output", Paths.DIR_SRC,
                    "--reject", Paths.DIR_TEMP + "patch_rejects\\");
        });

        copyJarAssets.configure(task -> {
            task.setGroup("decomp");
            task.setDescription("Copies the JAR assets into the src/main/resources folder.");
            task.from(project.zipTree(project.file(Paths.EXTRA_JAR)));
            task.into(project.file(Paths.DIR_RESOURCES));
            task.exclude("com/**", "paulscode/**");
        });

        copySrc.configure(task -> {
           task.setGroup("decomp");
           task.from(project.zipTree(project.file(Paths.FINAL_JAR)));
           task.into(project.file(project.getBuildDir().getAbsolutePath() + "\\backupSrc\\"));
        });

        execute.configure(task -> {
            List<String> taskList = new ArrayList<>();

            taskList.add(project.getTasks().getByName("stripJar").getName());
            taskList.add(project.getTasks().getByName("mcinject").getName());
            taskList.add(project.getTasks().getByName("deobfJar").getName());
            taskList.add(project.getTasks().getByName("decompile").getName());
            taskList.add(project.getTasks().getByName("unzip").getName());
            taskList.add(project.getTasks().getByName("patch").getName());
            taskList.add(project.getTasks().getByName("copyJarAssets").getName());
            taskList.add(project.getTasks().getByName("copySrc").getName());

            task.setTasks(taskList);
        });
    }
}
