package com.ancientmc.acp.test;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestRegistry {
    private final File jsonFile;

    public TestRegistry(File jsonFile) {
        this.jsonFile = jsonFile;
    }


    /**
     * @return The list of test objects that will be run.
     */
    public List<TestObject> get() {
        List<TestObject> list = new ArrayList<>();
        JsonObject json = getJson();
        JsonArray tests = json.getAsJsonArray("tests");

        tests.asList().forEach(e -> {
            JsonObject test = e.getAsJsonObject();
            list.add(getTest(test));
        });

        return list;
    }


    /**
     * @param test The JSON object for the test.
     * @return The test as a record object.
     */
    private TestObject getTest(JsonObject test) {
        String name = test.get("name").getAsString();
        String version = test.get("version").getAsString();
        String loader = test.get("loader").getAsString();
        List<String> tasks = getTasks(test.getAsJsonArray("tasks").asList());

        return new TestObject(name, version, loader, tasks);
    }


    /**
     * @param elements The JSON elements of all the tasks.
     * @return The test tasks as a String list.
     */
    public List<String> getTasks(List<JsonElement> elements) {
        List<String> tasks = new ArrayList<>();

        elements.forEach(e -> {
            JsonObject object = e.getAsJsonObject();
            String name = object.get("name").getAsString();
            tasks.add(name);
        });

        return tasks;
    }

    private JsonObject getJson() {
        try {
            Reader reader = new BufferedReader(new FileReader(jsonFile));
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
