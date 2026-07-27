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

public class BackpackControlConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 内置默认值，仅在生成 default.json 时使用
    public Map<String, List<int[]>> controlPoss = new HashMap<>() {{
        put("InventoryScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("CraftingScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("ContainerScreen",
                Arrays.asList(new int[]{139, 80}, new int[]{139, 12}));
        put("BlastFurnaceScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("FurnaceScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("SmokerScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("BeaconScreen",
                Collections.singletonList(new int[]{188, 96}));
        put("BrewingStandScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("CartographyTableScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("CrafterScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("DispenserScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("EnchantmentScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("GrindstoneScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("HopperScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("LoomScreen",
                Collections.singletonList(new int[]{105, 80}));
        put("MerchantScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("ShulkerBoxScreen",
                Arrays.asList(new int[]{139, 80}, new int[]{139, 12}));
        put("SmithingScreen",
                Collections.singletonList(new int[]{139, 81}));
        put("AnvilScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("StonecutterScreen",
                Collections.singletonList(new int[]{139, 80}));
        put("HorseInventoryScreen",
                Collections.singletonList(new int[]{139, 80}));
    }};

    /**
     * 在 control 配置文件夹内生成一个以 modId 命名的默认配置文件。
     * 如果文件已存在则跳过，不会覆盖用户或整合包的自定义内容。
     */
    public static void generateModDefaultConfig(String modId, Map<String, List<int[]>> defaultEntries) {
        Path controlDir = FabricLoader.getInstance().getConfigDir().resolve("yyzsbackpack/control");
        File targetFile = controlDir.resolve(modId + ".json").toFile();

        if (targetFile.exists()) {
            return;
        }

        BackpackControlConfig partial = new BackpackControlConfig();
        partial.controlPoss = new HashMap<>(defaultEntries);

        try {
            writeJsonToFile(partial, targetFile);
            Backpack.LOGGER.info("Generated default control config for mod '{}' at {}", modId, targetFile);
        } catch (IOException e) {
            Backpack.LOGGER.error("Failed to generate control config for mod '{}'", modId, e);
        }
    }

    /**
     * 从配置文件夹加载所有 .json 文件并合并。
     * 如果 default.json 不存在，则自动生成它
     * 合并顺序：文件夹内所有 .json 文件按文件名排序后依次叠加。
     */
    public static BackpackControlConfig loadConfig(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // 确保 default.json 始终存在
        File defaultFile = new File(configDir, "default.json");
        if (!defaultFile.exists()) {
            try {
                writeJsonToFile(new BackpackControlConfig(), defaultFile);
                Backpack.LOGGER.info("Generated default control config file at {}", defaultFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate default control config", e);
                // 如果写入失败，返回一个只包含内置默认值的对象
                return new BackpackControlConfig();
            }
        }

        // 加载所有 .json 文件
        Map<String, List<int[]>> merged = new HashMap<>();
        File[] jsonFiles = configDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
        if (jsonFiles != null) {
            Arrays.sort(jsonFiles, Comparator.comparing(File::getName));
            for (File file : jsonFiles) {
                try {
                    BackpackControlConfig partial = loadSingleFile(file);
                    if (partial != null) {
                        merged.putAll(partial.controlPoss);
                    }
                } catch (Exception e) {
                    Backpack.LOGGER.error("Failed to load control config from file: {}", file, e);
                }
            }
        }

        BackpackControlConfig config = new BackpackControlConfig();
        config.controlPoss = merged;
        return config;
    }

    public Map<String, List<int[]>> getControlPoss() {
        return this.controlPoss;
    }


    private static BackpackControlConfig loadSingleFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            return GSON.fromJson(br, BackpackControlConfig.class);
        }
    }

    private static void writeJsonToFile(BackpackControlConfig config, File file) throws IOException {
        Path filePath = file.toPath();
        Files.createDirectories(filePath.getParent());
        try (FileOutputStream stream = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }
}