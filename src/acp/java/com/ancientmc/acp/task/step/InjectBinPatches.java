package com.ancientmc.acp.task.step;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.util.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Injects LZMA files found within a specified directory into the Minecraft JAR.
 * Binary injection is done via Minecraft Forge's Binary Patcher. Multiple LZMA files can be injected, as this task
 * makes temporary JAR files for each implemented LZMA.
 */
public class InjectBinPatches extends Step {

    public InjectBinPatches(Project project, AcpLogger logger, String message) {
        setCore(project, logger, message);
    }

    /**
     * The base input JAR.
     */
    private File input;

    /**
     * The base output JAR.
     */
    private File output;

    /**
     * The patch directory containing the LZMA files.
     */
    private File patchDirectory;

    @Override
    public void action() throws IOException {
        List<File> files = getFiles(patchDirectory);

        files.forEach(lzma -> {
            File currIn = getCurrentInput(input, files, lzma);
            File currOut = getCurrentOutput(output, files, lzma);

            project.javaexec(action -> {
                Configuration binpatch = project.getConfigurations().findByName("binpatch");
                action.getMainClass().set("net.neoforged.binarypatcher.ConsoleTool");
                action.setClasspath(project.files(binpatch));
                action.args("--clean", currIn.getAbsolutePath(), "--apply", lzma.getAbsolutePath(), "--output", currOut.getAbsolutePath(), "--unpatched");
            });
        });

        FileUtils.deleteDirectory(project.file(Paths.DIR_TEMP + "modjars/"));
    }

    /**
     * Gets a filtered list of LZMA files from the patch directory. It is filtered to only include LZMAs, and
     * compared so that the mod loader LZMA (Risugami or Forge) is injected first.
     * @param directory The patch directory.
     * @return The LZMA list.
     */
    public List<File> getFiles(File directory) {
        return FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY)
                .stream().filter(f -> f.getName().endsWith(".lzma"))
                .sorted(new LzmaComparator())
                .collect(Collectors.toList());
    }

    /**
     * Gets the currently iterated input JAR. If the index value of the current LZMA in the LZMA list getting injected is 0, that indicates that no injection has ocurred,
     * meaning the input is simply the original vanilla jar input. If not, a temporary file is created with the current index value of the LZMA.
     * @param input The vanilla input JAR file (${version}-slim.jar).
     * @param files The list of LZMA patch files.
     * @param lzma The currently iterated LZMA.
     * @return The current input for injection.
     */
    public File getCurrentInput(File input, List<File> files, File lzma) {
        File temp = project.file(Paths.DIR_TEMP + "modjars/temp" + files.indexOf(lzma) + ".jar");
        return files.indexOf(lzma) == 0 ? input : temp;
    }

    /**
     * Similar to getCurrentInput(), we want to find the currently iterated value of the jar getting output.
     * If the index value in the lZMA list of the current LZMA getting injected is one less than the total size of the LZMA list,
     * that means that all LZMA files have been injected, and the final output can be returned. Otherwise, a new temp file is returned.
     * @param output The final output JAR file containing mod classes (${version}-mod.jar).
     * @param files The list of LZMA patch files.
     * @param lzma The currently iterated LZMA.
     * @return The current output after injection.
     */
    public File getCurrentOutput(File output, List<File> files, File lzma) {
        File temp = project.file(Paths.DIR_TEMP + "modjars/temp" + (files.indexOf(lzma) + 1) + ".jar");
        return files.indexOf(lzma) == files.size() - 1 ? output : temp;
    }

    /**
     * We want to compare the LZMA files to ensure that the ModLoader LZMA is the first element in the list, and therefore is injected
     * first. This comparator does that and gets called above for sorting.
     */
    private static class LzmaComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            return (o1.getName().contains("modloader") || o1.getName().contains("forge")) ? -1 : 0;
        }
    }

    public InjectBinPatches setOutput(File output) {
        this.output = output;
        return this;
    }

    public InjectBinPatches setInput(File input) {
        this.input = input;
        return this;
    }

    public InjectBinPatches setPatchDirectory(File patchDirectory) {
        this.patchDirectory = patchDirectory;
        return this;
    }
}
