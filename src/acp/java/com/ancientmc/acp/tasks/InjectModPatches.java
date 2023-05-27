package com.ancientmc.acp.tasks;

import com.ancientmc.acp.utils.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Injects LZMA files found within a specified directory into the Minecraft JAR.
 * Binary injection is done via Minecraft Forge's Binary Patcher. Multiple LZMA files
 */
public abstract class InjectModPatches extends DefaultTask {

    @TaskAction
    public void exec() {
        try {
            File input = getInputJar().get().getAsFile();
            File dir = getPatchDir().get().getAsFile();
            File output = getOutputJar().get().getAsFile();
            run(input, dir, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param input
     * @param dir
     * @param output
     */
    public void run(File input, File dir, File output) throws IOException {
        Project project = getProject();
        List<File> files = getFiles(dir);
        files.forEach(lzma -> {
            File currIn = getCurrentInput(input, files, lzma);
            File currOut = getCurrentOutput(output, files, lzma);
            project.javaexec(action -> {
                Configuration binpatcher = project.getConfigurations().getByName("binpatcher");
                action.getMainClass().set("net.minecraftforge.binarypatcher.ConsoleTool");
                action.setClasspath(project.files(binpatcher));
                action.args("--clean", currIn.getAbsolutePath(), "--apply", lzma.getAbsolutePath(), "--output", currOut.getAbsolutePath(), "--unpatched");
            });
        });
        FileUtils.deleteDirectory(project.file(Paths.DIR_TEMP + "modjars\\"));
    }

    public List<File> getFiles(File directory) {
        return FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY)
                .stream().filter(f -> f.getName().endsWith(".lzma"))
                .sorted(new LzmaComparator())
                .collect(Collectors.toList());
    }

    /**
     * Gets the currently iterated input JAR. If the index value of the current LZMA in the LZMA list getting injected is 0, that indicates that no injection has ocurred,
     * meaning the input is simply the original vanilla jar input. If not, a temporary file is created with the current index value of the LZMA.
     * @param input The vanilla input file (ver-srg.jar)
     * @param files The LZMA list.
     * @param lzma The currently iterated LZMA.
     * @return The current input for injection.
     */
    public static File getCurrentInput(File input, List<File> files, File lzma) {
        File temp = new File(Paths.DIR_TEMP, "modjars//temp" + files.indexOf(lzma) + ".jar");
        return files.indexOf(lzma) == 0 ? input : temp;
    }

    /**
     * Similar to getCurrentInput(), we want to find the currently iterated value of the jar getting output.
     * If the index value in the lZMA list of the current LZMA getting injected is one less than the total size of the LZMA list,
     * that means that all LZMA files have been injected, and the final output can be returned. Otherwise, a new temp file is returned.
     * @param output
     * @param files
     * @param lzma
     * @return
     */
    public static File getCurrentOutput(File output, List<File> files, File lzma) {
        File temp = new File(Paths.DIR_TEMP, "modjars//temp" + (files.indexOf(lzma) + 1) + ".jar");
        return files.indexOf(lzma) == files.size() - 1 ? output : temp;
    }

    /**
     * We want to compare the LZMA files to ensure that the ModLoader JAR is the first element in the list, and therefore is injected
     * first. This comparator does that and gets called above for sorting.
     */
    private static class LzmaComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().contains("modloader") ? -1 : 0;
        }
    }

    /**
     * The input JAR.
     */
    @InputFile
    public abstract RegularFileProperty getInputJar();

    /**
     * The directory containing the LZMA archive(s).
     */
    @InputDirectory
    public abstract RegularFileProperty getPatchDir();

    /**
     * The output JAR.
     */
    @OutputFile
    public abstract RegularFileProperty getOutputJar();
}