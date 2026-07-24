package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yyz.yyzsbackpack.Backpack;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BackpackUiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 内置默认值，仅在生成 default.json 时使用
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

    /**
     * 在 ui 配置文件夹内生成一个以 modId 命名的默认配置文件。
     * 如果文件已存在则跳过，不会覆盖用户或整合包的自定义内容。
     */
    public static void generateModDefaultConfig(String modId, Map<String, List<int[]>> defaultEntries) {
        Path uiDir = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/ui");
        File targetFile = uiDir.resolve(modId + ".json").toFile();

        if (targetFile.exists()) {
            return;
        }

        BackpackUiConfig partial = new BackpackUiConfig();
        partial.uiOffsets = new HashMap<>(defaultEntries);

        try {
            writeJsonToFile(partial, targetFile);
            Backpack.LOGGER.info("Generated default ui config for mod '{}' at {}", modId, targetFile);
        } catch (IOException e) {
            Backpack.LOGGER.error("Failed to generate ui config for mod '{}'", modId, e);
        }
    }

    /**
     * 从配置文件夹加载所有 .json 文件并合并。
     * 如果 default.json 不存在，则自动生成它
     * 合并顺序：文件夹内所有 .json 文件按文件名排序后依次叠加。
     */
    public static BackpackUiConfig loadConfig(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // 确保 default.json 始终存在
        File defaultFile = new File(configDir, "default.json");
        if (!defaultFile.exists()) {
            try {
                writeJsonToFile(new BackpackUiConfig(), defaultFile);
                Backpack.LOGGER.info("Generated default ui config file at {}", defaultFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate default ui config", e);
                return new BackpackUiConfig();
            }
        }

        // 加载所有 .json 文件
        Map<String, List<int[]>> merged = new HashMap<>();
        File[] jsonFiles = configDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
        if (jsonFiles != null) {
            Arrays.sort(jsonFiles, Comparator.comparing(File::getName));
            for (File file : jsonFiles) {
                try {
                    BackpackUiConfig partial = loadSingleFile(file);
                    if (partial != null) {
                        merged.putAll(partial.uiOffsets);
                    }
                } catch (Exception e) {
                    Backpack.LOGGER.error("Failed to load ui config from file: {}", file, e);
                }
            }
        }

        BackpackUiConfig config = new BackpackUiConfig();
        config.uiOffsets = merged;
        return config;
    }

    public Map<String, List<int[]>> getUiOffsets() {
        return this.uiOffsets;
    }


    private static BackpackUiConfig loadSingleFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            return GSON.fromJson(br, BackpackUiConfig.class);
        }
    }

    private static void writeJsonToFile(BackpackUiConfig config, File file) throws IOException {
        Path filePath = file.toPath();
        Files.createDirectories(filePath.getParent());
        try (FileOutputStream stream = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }
}