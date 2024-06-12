package com.ancientmc.modtools.tasks;

import com.ancientmc.acp.utils.Paths;
import net.minecraftforge.srgutils.IMappingFile;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Makes the SRG file for reobfuscation. It's not a TSRGv2, just a regular SRG, because then it can be read by SpecialSource,
 * which is what's used for reobfuscation. The SRG is made via converting from the main TSRGv2 found in the ACP data.
 * The conversion process is done using SRGUtils.
 */
public abstract class MakeReobfSrg extends DefaultTask {
    @TaskAction
    public void exec() {
        try {
            File input = getInputSrg().get().getAsFile();
            File output = getOutputSrg().get().getAsFile();
            File temp = getProject().file(Paths.DIR_TEMP + "temp.srg");

            IMappingFile.load(input).write(temp.toPath(), IMappingFile.Format.SRG, false);

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            List<String> lines = Files.readAllLines(temp.toPath());

            /*
             Adds a line to strip the package of any straggling classes with the "net/minecraft/src" package; since the
             vanilla classes are already accounted for in the SRG, by process of elimination this leaves mod classes who get put
             into the net/minecraft/src path.
             */
            writer.write("PK: . net/minecraft/src\n");

            for(String line : lines) {
                writer.write(line + "\n");
            }
            writer.close();
            FileUtils.forceDelete(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @InputFile
    public abstract RegularFileProperty getInputSrg();

    @OutputFile
    public abstract RegularFileProperty getOutputSrg();
}
