package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.logger.AcpLogger;
import com.ancientmc.acp.task.step.Step;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.internal.os.OperatingSystem;

import javax.tools.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Compiles the game using Java's Compiler API.
 */
public class JavaCompileStep extends Step {

    /**
     * The source directory.
     */
    private File sourceDirectory;

    /**
     * The classpath containing JAR libraries.
     */
    private FileCollection classpathCollection;

    /**
     * The directory containing native libraries.
     */
    private File nativesDirectory;

    /**
     * The output directory for our classes.
     */
    private File outputDirectory;

    public JavaCompileStep(Project project, AcpLogger logger, String message) {
        build(project, logger, message);
    }

    @Override
    public void action() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.ENGLISH, Charset.defaultCharset());
        Iterable<? extends JavaFileObject> sources = getSources(manager, sourceDirectory);
        String classpath = getClasspath(classpathCollection);
        compile(compiler, manager, sources, classpath);
    }

    /**
     * Compiles the game.
     * @param compiler The compiler.
     * @param manager The Java file manager.
     * @param sources Our sources.
     * @param classpath Our classpath.
     */
    public void compile(JavaCompiler compiler, JavaFileManager manager, Iterable<? extends JavaFileObject> sources, String classpath) {
        List<String> options = Arrays.asList(
                "-g:none", "-source", "8", "-target", "8",
                "-classpath", classpath, "-Xlint:none",
                "-d", outputDirectory.getAbsolutePath()
        );
        System.setProperty("java.library.path", nativesDirectory.getAbsolutePath());
        log(classpath, options);
        compiler.getTask(null, manager, null, options, null, sources).call();
    }

    public void log(String classpath, List<String> options) {
        logger.file(project, "Source directory -> {}", sourceDirectory.getAbsolutePath());
        logger.file(project, "Classpath -> {}", classpath);
        logger.file(project, "Output directory -> {}", outputDirectory.getAbsolutePath());
        logger.file(project, "Options -> {}", String.join(" ", options));
    }

    /**
     * Gets the sources as an iterable list of JavaFileObjects.
     * @param manager Java file manager.
     * @param sourceDirectory The main source directory ("src/main/java").
     * @return The sources, excluding the "acp/client" package path.
     */
    public Iterable<? extends JavaFileObject> getSources(StandardJavaFileManager manager, File sourceDirectory) {
        Collection<File> sources = FileUtils.listFiles(sourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        sources = sources.stream().filter(file -> !file.getParentFile().getParentFile().getName().equals("acp")).toList(); // stupid way to remove all ACP launch classes.
        return manager.getJavaFileObjects(sources.toArray(new File[0]));
    }

    /**
     * Gets the classpath libraries as a joined String separated by an OS-dependent delimiter.
     * @param classpathCollection The classpath collection.
     * @return The classpath.
     */
    public String getClasspath(FileCollection classpathCollection) {
        List<String> list = new ArrayList<>();
        classpathCollection.forEach(file -> list.add(file.getAbsolutePath()));
        String delimiter = OperatingSystem.current().isWindows() ? ";" : ":";
        return String.join(delimiter, list);
    }

    public JavaCompileStep setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        return this;
    }

    public JavaCompileStep setClasspathCollection(FileCollection classpathCollection) {
        this.classpathCollection = classpathCollection;
        return this;
    }

    public JavaCompileStep setNativesDirectory(File nativesDirectory) {
        this.nativesDirectory = nativesDirectory;
        return this;
    }

    public JavaCompileStep setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }
}
