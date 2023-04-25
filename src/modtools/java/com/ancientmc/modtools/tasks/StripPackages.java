package com.ancientmc.modtools.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Strips the "net.minecraft.src" package from any class that didn't already get it removed during reobfuscation.
 * AKA, strip packages of (ModLoader) mod class files and put them in the default class structure.
 * This task both physically moves the class file to the default package and strips the package bytecode from the class using ASM.
 */
public abstract class StripPackages extends DefaultTask {
    @TaskAction
    public void exec() {
        File inputDir = getClassDirectoryIn().get().getAsFile();
        File outputDir = getClassDirectoryOut().get().getAsFile();

        run(inputDir, outputDir);
    }

    public static void run(File inputDir, File outputDir) {
        Collection<File> classes = FileUtils.listFiles(inputDir, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
        List<File> modClasses = new ArrayList<>();
        classes.forEach(cls -> {
            if (cls.getPath().contains("net\\minecraft\\src\\")) {
                modClasses.add(cls);
            }
        });

        rename(modClasses, outputDir);
    }

    public static void rename(List<File> classes, File outputDir) {
        classes.forEach(cls -> {
            try {
                ClassReader reader = new ClassReader(new FileInputStream(cls));
                ClassWriter writer = new ClassWriter(0);
                ClassRemapper remapper = new ClassRemapper(writer, new PkgRemapper());
                reader.accept(remapper, 0);

                FileOutputStream out = new FileOutputStream(new File(outputDir, cls.getName()));
                out.write(writer.toByteArray());
                out.flush();

                cls.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class PkgRemapper extends Remapper {
        @Override
        public String map(String internalName) {
            return internalName.substring(internalName.lastIndexOf('/') + 1); // strips the class path of the package
        }
    }

    @InputDirectory
    public abstract DirectoryProperty getClassDirectoryIn();

    // Input and output directories are the same, making them distinct is to prevent any possible I/O jank.
    @OutputDirectory
    public abstract DirectoryProperty getClassDirectoryOut();
}
