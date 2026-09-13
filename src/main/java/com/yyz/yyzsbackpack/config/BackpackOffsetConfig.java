package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yyz.yyzsbackpack.Backpack;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BackpackOffsetConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile BackpackOffsetConfig INSTANCE;

    public Map<String, List<int[]>> offsetValues = new HashMap<>() {{
        put("InventoryScreen",    Collections.singletonList(new int[]{-180, 0}));
        put("CraftingScreen",     Collections.singletonList(new int[]{-180, 0}));
        put("BlastFurnaceScreen", Collections.singletonList(new int[]{-180, 0}));
        put("FurnaceScreen",      Collections.singletonList(new int[]{-180, 0}));
        put("SmokerScreen",       Collections.singletonList(new int[]{-180, 0}));
    }};

    private transient Map<String, File> sourceFiles = new HashMap<>();
    private transient File defaultFile;
    private transient File configDir;

    public static BackpackOffsetConfig getInstance() {
        return INSTANCE;
    }

    public static void generateModDefaultConfig(String modId, Map<String, List<int[]>> defaultEntries) {
        Path offsetDir = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/offset");
        File targetFile = offsetDir.resolve(modId + ".json").toFile();

        if (!targetFile.exists()) {
            BackpackOffsetConfig partial = new BackpackOffsetConfig();
            partial.offsetValues = new HashMap<>(defaultEntries);
            try {
                writeJsonToFile(partial, targetFile);
                Backpack.LOGGER.info("Generated default offset config for mod '{}' at {}", modId, targetFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate offset config for mod '{}'", modId, e);
            }
        }

        BackpackOffsetConfig inst = INSTANCE;
        if (inst != null && inst.offsetValues != null) {
            for (Map.Entry<String, List<int[]>> e : defaultEntries.entrySet()) {
                if (!inst.offsetValues.containsKey(e.getKey())) {
                    inst.offsetValues.put(e.getKey(), e.getValue());
                    if (inst.sourceFiles != null) {
                        inst.sourceFiles.put(e.getKey(), targetFile);
                    }
                }
            }
        }
    }

    public static BackpackOffsetConfig loadConfig(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File defaultFile = new File(configDir, "default.json");
        if (!defaultFile.exists()) {
            try {
                writeJsonToFile(new BackpackOffsetConfig(), defaultFile);
                Backpack.LOGGER.info("Generated default offset config file at {}", defaultFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate default offset config", e);
                BackpackOffsetConfig fallback = new BackpackOffsetConfig();
                fallback.defaultFile = defaultFile;
                fallback.configDir = configDir;
                INSTANCE = fallback;
                return fallback;
            }
        }

        Map<String, List<int[]>> merged = new HashMap<>();
        Map<String, File> sourceFiles = new HashMap<>();
        File[] jsonFiles = configDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
        if (jsonFiles != null) {
            Arrays.sort(jsonFiles, Comparator.comparing(File::getName));
            for (File file : jsonFiles) {
                try {
                    BackpackOffsetConfig partial = loadSingleFile(file);
                    if (partial != null && partial.offsetValues != null) {
                        merged.putAll(partial.offsetValues);
                        for (String key : partial.offsetValues.keySet()) {
                            sourceFiles.put(key, file);
                        }
                    }
                } catch (Exception e) {
                    Backpack.LOGGER.error("Failed to load offset config from file: {}", file, e);
                }
            }
        }

        BackpackOffsetConfig config = new BackpackOffsetConfig();
        config.offsetValues = merged;
        config.sourceFiles = sourceFiles;
        config.defaultFile = defaultFile;
        config.configDir = configDir;
        INSTANCE = config;
        return config;
    }

    public static BackpackOffsetConfig reload() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/offset");
        return loadConfig(dir.toFile());
    }

    public Map<String, List<int[]>> getOffsetValues() {
        return this.offsetValues;
    }

    public File getConfigDir() {
        return this.configDir;
    }

    public List<File> getAllFiles() {
        List<File> result = new ArrayList<>();
        if (configDir != null && configDir.exists()) {
            File[] files = configDir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".json"));
            if (files != null) {
                List<File> list = new ArrayList<>(Arrays.asList(files));
                list.sort(Comparator.comparing(File::getName));
                result.addAll(list);
            }
        }
        return result;
    }

    public List<String> getKeysFromFile(File file) {
        if (file == null || !file.exists()) return Collections.emptyList();
        try {
            BackpackOffsetConfig partial = loadSingleFile(file);
            if (partial == null || partial.offsetValues == null) return Collections.emptyList();
            List<String> keys = new ArrayList<>(partial.offsetValues.keySet());
            Collections.sort(keys);
            return keys;
        } catch (IOException e) {
            Backpack.LOGGER.error("Failed to read keys from {}", file, e);
            return Collections.emptyList();
        }
    }

    public List<int[]> getValueFromFile(File file, String key) {
        List<int[]> copy = new ArrayList<>();
        if (file == null || !file.exists()) return copy;
        try {
            BackpackOffsetConfig partial = loadSingleFile(file);
            if (partial == null || partial.offsetValues == null) return copy;
            List<int[]> v = partial.offsetValues.get(key);
            if (v == null) return copy;
            for (int[] a : v) copy.add(a.clone());
        } catch (IOException e) {
            Backpack.LOGGER.error("Failed to read value for {} from {}", key, file, e);
        }
        return copy;
    }

    public void saveKeyTo(String key, List<int[]> values, File file) throws IOException {
        if (file == null) throw new IOException("null target file");

        BackpackOffsetConfig partial;
        if (file.exists()) {
            partial = loadSingleFile(file);
            if (partial == null) partial = new BackpackOffsetConfig();
            if (partial.offsetValues == null) partial.offsetValues = new HashMap<>();
        } else {
            partial = new BackpackOffsetConfig();
            partial.offsetValues = new HashMap<>();
        }

        partial.offsetValues.put(key, values);
        writeJsonToFile(partial, file);

        if (this.offsetValues == null) this.offsetValues = new HashMap<>();
        this.offsetValues.put(key, values);
        if (sourceFiles == null) sourceFiles = new HashMap<>();
        sourceFiles.put(key, file);
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