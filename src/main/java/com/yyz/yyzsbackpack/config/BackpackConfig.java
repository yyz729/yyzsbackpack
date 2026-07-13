package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yyz.yyzsbackpack.client.BackpackKeyBinding;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BackpackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    public boolean model = true;

    public Map<String, List<int[]>> controlOffsets = new HashMap<>() {{
        put("net.minecraft.client.gui.screens.inventory.InventoryScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.CraftingScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.ContainerScreen",
                Arrays.asList(new int[]{0, 0}, new int[]{0, 0}));
    }};


    public static BackpackConfig loadConfig(File file) {
        BackpackConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                config = GSON.fromJson(bufferedReader, BackpackConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            config = new BackpackConfig();
        }

        config.saveConfig(file);

        return config;
    }

    public void saveConfig(File config) {
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
