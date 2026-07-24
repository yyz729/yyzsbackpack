package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BackpackMainConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean model = true;
    public int heavy = 2;

    public static BackpackMainConfig loadConfig(File file) {
        BackpackMainConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                config = GSON.fromJson(bufferedReader, BackpackMainConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            config = new BackpackMainConfig();
        }

        config.saveConfig(file);

        return config;
    }

    public void saveConfig(File config) {
        Path configPath = config.toPath();
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create directory: " + config.getParent(), e);
        }

        try (
                FileOutputStream stream = new FileOutputStream(config);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        ) {
            GSON.toJson(this, writer);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }
}
