package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.Paths;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * ACP uses AutoRenamingTool as its program to deobfuscate Minecraft's source code. The issue with ART is that it does not allow
 * package mapping, which becomes a problem when the classes for old ModLoader by Risugami have no packages. This class uses SpecialSource,
 * which does allow package mapping, to remap any unpackaged default classes to the "minecraft/src/" namespace. This is done by generating a temporary
 * SRG (not TSRG) file.
 */
public class RepackageDefaults extends JavaExecStep {

    /**
     * The TSRG file, used to figure out the namespace to assign in the generated package SRG.
     */
    protected File tsrg;

    public RepackageDefaults(Project project, AcpLogger logger, String message) {
        super(project, logger, message);
    }

    @Override
    public void action() {
        super.action();
    }

    /**
     * Writes the SRG file.
     */
    public File getSrg() {
        File srg = project.file(Paths.DIR_TEMP + "pkg.srg");

        try {
            FileUtils.write(srg, "PK: . " + getPackage(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return srg;
    }

    /**
     * We have to check the TSRG to see which package namespace is in use. Old versions used the "com/mojang/minecraft" namespace,
     * while newer ones use "net/minecraft".
     * @return The ../src/ package, dependent on the version.
     */
    public String getPackage() throws IOException {
        List<String> lines = Files.readAllLines(tsrg.toPath());
        return lines.stream().anyMatch(l -> l.contains("com/mojang/minecraft/")) ? "com/mojang/minecraft/src" : "net/minecraft/src";
    }

    public RepackageDefaults setConfiguration(String configuration) {
        super.setConfiguration(configuration);
        return this;
    }

    public RepackageDefaults setMainClass(String mainClass) {
        super.setMainClass(mainClass);
        return this;
    }

    public RepackageDefaults setTsrg(File tsrg) {
        this.tsrg = tsrg;
        return this;
    }

    /**
     * Hardcodes the SpecialSource arguments so we can include our generated SRG file.
     */
    public RepackageDefaults setArgs() {
        super.setArgs(Arrays.asList("--in-jar", Paths.MAPPED_JAR, "--out-jar", Paths.REPACKAGED_JAR, "--srg-in", getSrg().getAbsolutePath()));
        return this;
    }
}
