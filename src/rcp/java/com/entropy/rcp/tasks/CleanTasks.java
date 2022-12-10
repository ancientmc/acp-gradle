package com.entropy.rcp.tasks;

import com.entropy.rcp.utils.Paths;
import org.gradle.api.Project;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileFilter;

public class CleanTasks {
    public abstract class CleanTemp extends Delete {
        @TaskAction
        public void cleanTemp() {
            File[] commonFiles = CommonFiles.getFiles();
            for(File file : commonFiles) {
                delete(file);
            }
        }
    }

    public abstract class CleanAll extends Delete {
        @TaskAction
        public void cleanAll() {
            FileFilter gitIgnoreFilter = (File file) -> !file.getName().endsWith(".gitignore");
            File[] commonFiles = CommonFiles.getFiles();
            for(File file : commonFiles) {
                delete(file);
            }
            // TODO: This block can definitely get simplified somehow, especially with all the folders in the run directory.
            delete((Object) getProject().file(Paths.RCP_DIR_RESOURCES).listFiles(gitIgnoreFilter));
            delete(getProject().file(Paths.RCP_DIR_SRC + "net\\"));
            delete(getProject().file(Paths.RCP_DIR_RUN + "resources"));
            delete(getProject().file(Paths.RCP_DIR_RUN + "saves"));
            delete(getProject().file(Paths.RCP_DIR_RUN + "texturepacks"));
            delete(getProject().file(Paths.RCP_DIR_RUN + "options.txt"));


        }
    }

    public static class CommonFiles {
        static File[] files = {
                new File(Paths.RCP_DIR_TEMP + "*.jar"),
                new File(Paths.RCP_DIR_TEMP + "*.json"),
                new File(Paths.RCP_DIR_NATIVES + "*.jar"),
                new File(Paths.RCP_DIR_NATIVES + "META_INF\\")
        };
        public static File[] getFiles() {
            return files;
        }
    }
}
