package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BackpackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean model = true;
    public int heavy = 2;

    public Map<String, List<int[]>> controlPoss = new HashMap<>() {{
        put("net.minecraft.client.gui.screens.inventory.InventoryScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.CraftingScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.ContainerScreen",
                Arrays.asList(new int[]{139, 80}, new int[]{139, 12}));
        put("net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.FurnaceScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.SmokerScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.BeaconScreen",
                Collections.singletonList(new int[]{188, 96}));
        put("net.minecraft.client.gui.screens.inventory.BrewingStandScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.CartographyTableScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.CrafterScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.DispenserScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.EnchantmentScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.GrindstoneScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.HopperScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.LoomScreen",
                Collections.singletonList(new int[]{105, 80}));
        put("net.minecraft.client.gui.screens.inventory.MerchantScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.SmithingScreen",
                Collections.singletonList(new int[]{139, 81}));
        put("net.minecraft.client.gui.screens.inventory.AnvilScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.StonecutterScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.NautilusInventoryScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("net.minecraft.client.gui.screens.inventory.HorseInventoryScreen",
                Collections.singletonList(new int[]{139, 80}));
    }};

    public Map<String, List<int[]>> uiOffsets = new HashMap<>() {{
        put("net.minecraft.client.gui.screens.inventory.InventoryScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.CraftingScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.ContainerScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.FurnaceScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.SmokerScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.BeaconScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.BrewingStandScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.CartographyTableScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.CrafterScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.DispenserScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.EnchantmentScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.GrindstoneScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.HopperScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.LoomScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.MerchantScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.SmithingScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.AnvilScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.StonecutterScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.NautilusInventoryScreen",
                Collections.singletonList(new int[]{0, 0}));
        put("net.minecraft.client.gui.screens.inventory.HorseInventoryScreen",
                Collections.singletonList(new int[]{0, 0}));
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
