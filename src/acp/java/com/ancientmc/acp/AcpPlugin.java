package com.ancientmc.acp;

import com.ancientmc.acp.init.AcpInitializer;
import com.ancientmc.acp.tasks.InjectModPatches;
import com.ancientmc.acp.tasks.MakeHashes;
import com.ancientmc.acp.tasks.RepackageDefaults;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class AcpPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        String minecraftVersion = project.getExtensions().getExtraProperties().get("MC_VERSION").toString();
        AcpExtension extension = project.getExtensions().create("acp", AcpExtension.class, project);

        // Set the Minecraft version for the various directory/file paths to utilize.
        Paths.init(minecraftVersion);

        project.getPluginManager().apply(JavaPlugin.class);

        TaskProvider<JavaExec> stripJar = project.getTasks().register("stripJar", JavaExec.class);
        TaskProvider<InjectModPatches> injectModPatches = project.getTasks().register("injectModPatches", InjectModPatches.class);
        TaskProvider<JavaExec> mcinject = project.getTasks().register("mcinject", JavaExec.class);
        TaskProvider<JavaExec> deobfJar = project.getTasks().register("deobfJar", JavaExec.class);
        TaskProvider<JavaExec> decompileJar = project.getTasks().register("decompileJar", JavaExec.class);
        TaskProvider<Copy> unzip = project.getTasks().register("unzip", Copy.class);
        TaskProvider<JavaExec> patch = project.getTasks().register("patch", JavaExec.class);
        TaskProvider<RepackageDefaults> repackageDefaults = project.getTasks().register("repackageDefaults", RepackageDefaults.class);
        TaskProvider<Copy> copyResources = project.getTasks().register("copyResources", Copy.class);
        TaskProvider<Copy> backupSrc = project.getTasks().register("backupSrc", Copy.class);
        TaskProvider<Copy> backupResources = project.getTasks().register("backupResources", Copy.class);
        TaskProvider<JavaCompile> testCompile = project.getTasks().register("testCompile", JavaCompile.class);
        TaskProvider<MakeHashes> makeVanillaHashes = project.getTasks().register("makeVanillaHashes", MakeHashes.class);

        Configuration jarsplitter = project.getConfigurations().create("jarsplitter");
        Configuration mcinjector = project.getConfigurations().create("mcinjector");
        Configuration forgeart = project.getConfigurations().create("forgeart");
        Configuration fernflower = project.getConfigurations().create("fernflower");
        Configuration diffpatch = project.getConfigurations().create("diffpatch");
        Configuration binpatch = project.getConfigurations().create("binpatch");

        project.afterEvaluate(proj -> {
            try {
                AcpInitializer.init(proj, extension, minecraftVersion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        stripJar.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Strips the JAR into two, one JAR containing the core Minecraft classes, and the other containing everything else.");
            task.getMainClass().set("net.neoforged.jarsplitter.ConsoleTool");
            task.setClasspath(project.files(jarsplitter));
            task.args("--input", Paths.BASE_JAR, "--slim", Paths.SLIM_JAR, "--extra", Paths.EXTRA_JAR, "--srg", Paths.SRG);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        File modPatches = project.file(Paths.DIR_MODPATCHES);

        injectModPatches.configure(task -> {
            task.setGroup("decompile");
            task.dependsOn(stripJar);
            task.getInputJar().set(project.file(Paths.SLIM_JAR));
            task.getPatchDir().set(modPatches);
            task.getOutputJar().set(project.file(Paths.MODLOADER_JAR));
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        boolean vanilla = !modPatches.exists(); // has the downloadModLoader task been run (and thus the modpatches dir created)? If not, assume vanilla workspace
        String toInject = vanilla ? Paths.SLIM_JAR : Paths.MODLOADER_JAR; // the JAR path to inject with MCInjector, depending on the vanilla status
        String dependent = vanilla ? "stripJar" : "injectModPatches"; // the task to run before the MCInject task, depending on the vanilla status.

        mcinject.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Injects the slim JAR with local variables, exceptions, and other stuff to eliminate errors.");
            task.dependsOn(project.getTasks().getByName(dependent));
            task.getMainClass().set("de.oceanlabs.mcp.mcinjector.MCInjector");
            task.setClasspath(project.files(mcinjector));
            task.args("--in", toInject, "--out", Paths.INJECT_JAR, "--exc", Paths.DIR_MAPPINGS + "exceptions.txt", "--blacklist", Paths.DIR_MAPPINGS + "blacklist.txt");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        deobfJar.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Deobfuscates the JAR with human-readable names.");
            task.dependsOn(mcinject);
            task.getMainClass().set("net.neoforged.art.Main");
            task.setClasspath(project.files(forgeart));
            task.args("--input", Paths.INJECT_JAR, "--output", Paths.SRG_JAR, "--map", Paths.SRG, "--src-fix", "--strip-sigs");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        decompileJar.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Decompiles the JAR.");
            task.dependsOn(deobfJar);
            task.getMainClass().set("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler");
            task.setClasspath(project.files(fernflower));
            task.args("-rbr=0", "-rsy=0", "-asc=1", "-din=1", "-dgs=0", "-jvn=1", Paths.SRG_JAR, Paths.FINAL_JAR);
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        unzip.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Unzips the java source files into the src directory.");
            task.dependsOn(decompileJar);
            task.from(project.zipTree(project.file(Paths.FINAL_JAR)));
            task.into(project.file(Paths.DIR_SRC));
        });

        patch.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Patches the source files to make the game able to compile.");
            task.dependsOn(unzip);
            task.getMainClass().set("codechicken.diffpatch.DiffPatch");
            task.setClasspath(project.files(diffpatch));
            task.args("--patch", Paths.DIR_SRC, Paths.DIR_PATCHES, "--output", Paths.DIR_SRC,
                    "--reject", Paths.DIR_TEMP + "patch_rejects/");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        repackageDefaults.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Repackages any default source files into the net/minecraft/src directory.");
            task.dependsOn(patch);
            task.getSourceDirIn().set(project.file(Paths.DIR_SRC));
            task.getSourceDirOut().set(project.file(Paths.DIR_SRC));
        });

        copyResources.configure(task -> {
            task.setGroup("decompile");
            task.setDescription("Copies the JAR assets into the src/main/resources folder.");
            task.dependsOn(repackageDefaults);
            task.from(project.zipTree(project.file(Paths.EXTRA_JAR)));
            task.into(project.file(Paths.DIR_RESOURCES));
            task.exclude("com/**", "paulscode/**", "META-INF/**");
        });

        backupSrc.configure(task -> {
           task.setGroup("decompile");
           task.dependsOn(copyResources);
           task.from(project.file(Paths.DIR_SRC)).exclude("acp/");
           task.into(project.file(Paths.DIR_VANILLA_SRC));
        });

        backupResources.configure(task -> {
            task.setGroup("decompile");
            task.dependsOn(backupSrc);
            task.from(project.file(Paths.DIR_RESOURCES));
            task.into(project.file(Paths.DIR_VANILLA_RESOURCES));
        });

        testCompile.configure(task -> {
            task.setGroup("decompile");
            task.dependsOn(backupResources);
            task.setSource(project.file(Paths.DIR_SRC));
            task.setClasspath(project.getExtensions().getByType(SourceSetContainer.class).getByName("main").getCompileClasspath());
            task.getDestinationDirectory().set(new File(Paths.DIR_VANILLA_CLASSES));
            task.getOptions().setCompilerArgs(Arrays.asList("-g:none", "-source", "1.6", "-target", "1.6"));
            task.exclude("acp/");
            task.getLogging().captureStandardOutput(LogLevel.DEBUG);
        });

        makeVanillaHashes.configure(task -> {
            task.setGroup("decompile");
            task.dependsOn(testCompile);
            task.getClassesDirectory().set(project.file(Paths.DIR_VANILLA_CLASSES));
            task.getResourcesDirectory().set(project.file(Paths.DIR_VANILLA_RESOURCES));
            task.getOutput().set(project.file("build/modding/hashes/vanilla.md5"));
        });
    }
}
