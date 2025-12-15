package com.ancientmc.acp.task;

import com.ancientmc.acp.util.AcpException;
import com.ancientmc.acp.util.Util;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lists the supported versions for the AncientCoderPack in the console.
 */
public abstract class ListSupportedVersions extends AcpTask {

    @TaskAction
    public void exec() {
        setLogger();

        try {
            String xmlUrl = getXmlUrl().get();
            List<String> versions = getVersions(Util.getUrl(xmlUrl));
            versions = versions.stream().sorted(new LegacyAlphaLastComparator()).toList();

            log(versions);
            logger.write();
        } catch (IOException e) {
            throw new AcpException("Version XML parsing error.", logger, getProject(), e);
        }
    }

    private static List<String> getVersions(URL url) throws IOException {
        List<String> versions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("<version>") && line.contains("</version>")) {
                    String version = line.substring(line.indexOf('>') + 1, line.lastIndexOf('<'));
                    versions.add(version);
                }
            }
        }

        return versions;
    }

    public void log(List<String> versions) {
        logger.console(getProject(), "{} supported versions", Integer.toString(versions.size()));
        versions.forEach(ver -> logger.console(getProject(), "{}", ver));
    }

    /**
     * Ensures the legacy alpha version is listed last. Otherwise, it shows up in a weird spot in the list.
     */
    private static class LegacyAlphaLastComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.equals("a1.2.6-legacy") ? 0 : -1;
        }
    }

    @Input
    public abstract Property<String> getXmlUrl();
}