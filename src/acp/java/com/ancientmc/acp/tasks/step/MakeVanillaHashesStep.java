package com.ancientmc.acp.tasks.step;

import com.ancientmc.acp.tasks.MakeHashes;
import com.ancientmc.acp.util.Paths;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class MakeVanillaHashesStep extends Step {

    /**
     * The gradle project.
     */
    protected Project project;

    @Override
    public void exec(Logger logger, boolean condition) {
        super.exec(logger, condition);

        MakeHashes makeHashes = project.getTasks().register("makeVanillaHashes", MakeHashes.class).getOrNull();

        if (makeHashes != null) {
            makeHashes.getClassesDirectory().set(project.file(Paths.DIR_VANILLA_CLASSES));
            makeHashes.getResourcesDirectory().set(project.file(Paths.DIR_VANILLA_RESOURCES));
            makeHashes.getOutput().set(project.file("build/modding/hashes/vanilla.md5"));

            makeHashes.getActions().forEach(action -> action.execute(makeHashes));
        }
    }

    public MakeVanillaHashesStep setProject(Project project) {
        this.project = project;
        return this;
    }
}
