package com.ancientmc.acp.task.step.function;

import com.ancientmc.acp.task.step.Step;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.internal.os.OperatingSystem;

import javax.tools.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class JavaCompileStep extends Step {

    /**
     * The source directory.
     */
    protected File sourceDirectory;

    /**
     * The classpath containing JAR libraries.
     */
    protected FileCollection classpathCollection;

    /**
     * The directory containing native libraries.
     */
    protected File nativesDirectory;

    /**
     * The output directory for our classes.
     */
    protected File outputDirectory;

    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.ENGLISH, Charset.defaultCharset());
        Iterable<? extends JavaFileObject> sources = getSources(manager, sourceDirectory);
        String classpath = getClasspath(classpathCollection);

        compile(compiler, manager, sources, classpath);
    }

    public void compile(JavaCompiler compiler, JavaFileManager manager, Iterable<? extends JavaFileObject> sources, String classpath) {
        List<String> options = Arrays.asList(
                "-g:none", "-source", "8", "-target", "8",
                "-classpath", classpath,
                "-d", outputDirectory.getAbsolutePath()
        );
        System.setProperty("java.library.path", nativesDirectory.getAbsolutePath());

        compiler.getTask(null, manager, null, options, null, sources).call();
    }

    public Iterable<? extends JavaFileObject> getSources(StandardJavaFileManager manager, File sourceDirectory) {
        Collection<File> sources = FileUtils.listFiles(sourceDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        return manager.getJavaFileObjects(sources.toArray(new File[0]));
    }

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
