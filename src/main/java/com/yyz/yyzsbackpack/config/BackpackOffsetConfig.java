package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yyz.yyzsbackpack.Backpack;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 配置背包按钮在带配方书的界面（如物品栏、工作台）上的偏移量。
 * 配置文件位于 config/yyzsbackpack/offset/ 目录下。
 */
public class BackpackOffsetConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 内置默认值
    public Map<String, List<int[]>> offsetValues = new HashMap<>() {{
        put("InventoryScreen",
                Collections.singletonList(new int[]{-180, 0}));
        put("CraftingScreen",
                Collections.singletonList(new int[]{-180, 0}));
        put("BlastFurnaceScreen",
                Collections.singletonList(new int[]{-180, 0}));
        put("FurnaceScreen",
                Collections.singletonList(new int[]{-180, 0}));
        put("SmokerScreen",
                Collections.singletonList(new int[]{-180, 0}));
    }};

    /**
     * 在 offset 配置文件夹内生成一个以 modId 命名的默认配置文件。
     * 如果文件已存在则跳过，不会覆盖用户或整合包的自定义内容。
     */
    public static void generateModDefaultConfig(String modId, Map<String, List<int[]>> defaultEntries) {
        Path offsetDir = FabricLoader.getInstance().getConfigDir().resolve("yyzsbackpack/offset");
        File targetFile = offsetDir.resolve(modId + ".json").toFile();

        if (targetFile.exists()) {
            return;
        }

        BackpackOffsetConfig partial = new BackpackOffsetConfig();
        partial.offsetValues = new HashMap<>(defaultEntries);

        try {
            writeJsonToFile(partial, targetFile);
            Backpack.LOGGER.info("Generated default offset config for mod '{}' at {}", modId, targetFile);
        } catch (IOException e) {
            Backpack.LOGGER.error("Failed to generate offset config for mod '{}'", modId, e);
        }
    }

    /**
     * 从配置文件夹加载所有 .json 文件并合并。
     * 如果 default.json 不存在，则自动生成它。
     * 合并顺序：文件夹内所有 .json 文件按文件名排序后依次叠加。
     */
    public static BackpackOffsetConfig loadConfig(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // 确保 default.json 始终存在
        File defaultFile = new File(configDir, "default.json");
        if (!defaultFile.exists()) {
            try {
                writeJsonToFile(new BackpackOffsetConfig(), defaultFile);
                Backpack.LOGGER.info("Generated default offset config file at {}", defaultFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate default offset config", e);
                return new BackpackOffsetConfig();
            }
        }

        // 加载所有 .json 文件
        Map<String, List<int[]>> merged = new HashMap<>();
        File[] jsonFiles = configDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
        if (jsonFiles != null) {
            Arrays.sort(jsonFiles, Comparator.comparing(File::getName));
            for (File file : jsonFiles) {
                try {
                    BackpackOffsetConfig partial = loadSingleFile(file);
                    if (partial != null) {
                        merged.putAll(partial.offsetValues);
                    }
                } catch (Exception e) {
                    Backpack.LOGGER.error("Failed to load offset config from file: {}", file, e);
                }
            }
        }

        BackpackOffsetConfig config = new BackpackOffsetConfig();
        config.offsetValues = merged;
        return config;
    }

    public Map<String, List<int[]>> getOffsetValues() {
        return this.offsetValues;
    }

    private static BackpackOffsetConfig loadSingleFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            return GSON.fromJson(br, BackpackOffsetConfig.class);
        }
    }

    private static void writeJsonToFile(BackpackOffsetConfig config, File file) throws IOException {
        Path filePath = file.toPath();
        Files.createDirectories(filePath.getParent());
        try (FileOutputStream stream = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }
}