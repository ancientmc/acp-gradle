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

public abstract class MakeReobfSrg extends DefaultTask {
    @TaskAction
    public void exec() {
        try {
            File input = getInputSrg().get().getAsFile();
            File output = getOutputSrg().get().getAsFile();
            File temp = new File(Paths.DIR_TEMP, "temp.srg");

            IMappingFile.load(input).write(temp.toPath(), IMappingFile.Format.SRG, false);

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            List<String> lines = Files.readAllLines(temp.toPath());
            writer.write("PK: . net/minecraft/src\n");
            for(String line : lines) {
                writer.write(line + '\n');
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
