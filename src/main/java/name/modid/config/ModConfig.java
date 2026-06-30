package name.modid.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

//Config file is called "origin-buildcrafts-own.json"

public class ModConfig {

    public static final Path FILE = Path.of("config/origin-buildcrafts-own.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enable_giant = true;
    public boolean enable_jumper = true;

    public static ModConfig INSTANCE = new ModConfig();

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                save();
                return;
            }

            Reader reader = Files.newBufferedReader(FILE);
            INSTANCE = GSON.fromJson(reader, ModConfig.class);
            reader.close();

        } catch (Exception e) {
            INSTANCE = new ModConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            Writer writer = Files.newBufferedWriter(FILE);
            GSON.toJson(INSTANCE, writer);
            writer.close();

        } catch (Exception ignored) {}
    }
}