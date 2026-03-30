package com.ancientmc.acp.task.step.mod;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import com.ancientmc.acp.util.Paths;
import net.neoforged.srgutils.IMappingFile;
import org.gradle.api.Project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Makes the SRG file needed for reobfuscating the class files.
 * @author moist-mason
 */
public class MakeReobfSrg extends Step {
    private File input;

    private File output;

    public MakeReobfSrg(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    @Override
    public void action() throws IOException {
        File temp = project.file(Paths.DIR_TEMP + "temp.srg");
        IMappingFile.load(input).reverse().write(temp.toPath(), IMappingFile.Format.SRG, false);
        logger.functions().fileToFile(input, output);
        BufferedWriter writer = Files.newBufferedWriter(output.toPath());
        List<String> lines = Files.readAllLines(temp.toPath());

        /*
         * Adds lines to strip the package of any straggling classes with the "net/minecraft/src" or the "com/mojang/minecraft/src/" packages.
         * Since the vanilla classes are already accounted for in the SRG, by process of elimination this leaves mod classes who get put
         * into the /src/ path.
         */
        writer.write("PK: net/minecraft/src .\n");
        writer.write("PK: com/mojang/minecraft/src .\n");

        for (String line : lines) {
            writer.write(line + "\n");
        }

        writer.close();
        Files.delete(temp.toPath());
    }

    public MakeReobfSrg setInput(File input) {
        this.input = input;
        return this;
    }

    public MakeReobfSrg setOutput(File output) {
        this.output = output;
        return this;
    }
}
