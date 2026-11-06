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

    private static volatile BackpackControlConfig INSTANCE;

    public Map<String, List<int[]>> controlPoss = new HashMap<>() {{
        put("InventoryScreen",          Collections.singletonList(new int[]{139, 80}));
        put("CraftingScreen",           Collections.singletonList(new int[]{139, 80}));
        put("ContainerScreen",          Arrays.asList(new int[]{139, 80}, new int[]{139, 12}));
        put("BlastFurnaceScreen",       Collections.singletonList(new int[]{139, 80}));
        put("FurnaceScreen",            Collections.singletonList(new int[]{139, 80}));
        put("SmokerScreen",             Collections.singletonList(new int[]{139, 80}));
        put("BeaconScreen",             Collections.singletonList(new int[]{188, 96}));
        put("BrewingStandScreen",       Collections.singletonList(new int[]{139, 80}));
        put("CartographyTableScreen",   Collections.singletonList(new int[]{139, 80}));
        put("CrafterScreen",            Collections.singletonList(new int[]{139, 80}));
        put("DispenserScreen",          Collections.singletonList(new int[]{139, 80}));
        put("EnchantmentScreen",        Collections.singletonList(new int[]{139, 80}));
        put("GrindstoneScreen",         Collections.singletonList(new int[]{139, 80}));
        put("HopperScreen",             Collections.singletonList(new int[]{139, 80}));
        put("LoomScreen",               Collections.singletonList(new int[]{105, 80}));
        put("MerchantScreen",           Collections.singletonList(new int[]{139, 80}));
        put("ShulkerBoxScreen",         Arrays.asList(new int[]{139, 80}, new int[]{139, 12}));
        put("SmithingScreen",           Collections.singletonList(new int[]{139, 81}));
        put("AnvilScreen",              Collections.singletonList(new int[]{139, 80}));
        put("StonecutterScreen",        Collections.singletonList(new int[]{139, 80}));
        put("NautilusInventoryScreen",  Collections.singletonList(new int[]{139, 80}));
        put("HorseInventoryScreen",     Collections.singletonList(new int[]{139, 80}));
    }};

    private transient Map<String, File> sourceFiles = new HashMap<>();
    private transient File defaultFile;
    private transient File configDir;

    public static BackpackControlConfig getInstance() {
        return INSTANCE;
    }

    public static void generateModDefaultConfig(String modId, Map<String, List<int[]>> defaultEntries) {
        Path controlDir = FabricLoader.getInstance().getConfigDir().resolve("yyzsbackpack/control");
        File targetFile = controlDir.resolve(modId + ".json").toFile();

        if (!targetFile.exists()) {
            BackpackControlConfig partial = new BackpackControlConfig();
            partial.controlPoss = new HashMap<>(defaultEntries);
            try {
                writeJsonToFile(partial, targetFile);
                Backpack.LOGGER.info("Generated default control config for mod '{}' at {}", modId, targetFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate control config for mod '{}'", modId, e);
            }
        }

        BackpackControlConfig inst = INSTANCE;
        if (inst != null && inst.controlPoss != null) {
            for (Map.Entry<String, List<int[]>> e : defaultEntries.entrySet()) {
                if (!inst.controlPoss.containsKey(e.getKey())) {
                    inst.controlPoss.put(e.getKey(), e.getValue());
                    if (inst.sourceFiles != null) {
                        inst.sourceFiles.put(e.getKey(), targetFile);
                    }
                }
            }
        }
    }

    public static BackpackControlConfig loadConfig(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File defaultFile = new File(configDir, "default.json");
        if (!defaultFile.exists()) {
            try {
                writeJsonToFile(new BackpackControlConfig(), defaultFile);
                Backpack.LOGGER.info("Generated default control config file at {}", defaultFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate default control config", e);
                BackpackControlConfig fallback = new BackpackControlConfig();
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
                    BackpackControlConfig partial = loadSingleFile(file);
                    if (partial != null && partial.controlPoss != null) {
                        merged.putAll(partial.controlPoss);
                        for (String key : partial.controlPoss.keySet()) {
                            sourceFiles.put(key, file);
                        }
                    }
                } catch (Exception e) {
                    Backpack.LOGGER.error("Failed to load control config from file: {}", file, e);
                }
            }
        }

        BackpackControlConfig config = new BackpackControlConfig();
        config.controlPoss = merged;
        config.sourceFiles = sourceFiles;
        config.defaultFile = defaultFile;
        config.configDir = configDir;
        INSTANCE = config;
        return config;
    }

    public static BackpackControlConfig reload() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("yyzsbackpack/control");
        return loadConfig(dir.toFile());
    }

    public Map<String, List<int[]>> getControlPoss() {
        return this.controlPoss;
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
            BackpackControlConfig partial = loadSingleFile(file);
            if (partial == null || partial.controlPoss == null) return Collections.emptyList();
            List<String> keys = new ArrayList<>(partial.controlPoss.keySet());
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
            BackpackControlConfig partial = loadSingleFile(file);
            if (partial == null || partial.controlPoss == null) return copy;
            List<int[]> v = partial.controlPoss.get(key);
            if (v == null) return copy;
            for (int[] a : v) copy.add(a.clone());
        } catch (IOException e) {
            Backpack.LOGGER.error("Failed to read value for {} from {}", key, file, e);
        }
        return copy;
    }

    public void saveKeyTo(String key, List<int[]> values, File file) throws IOException {
        if (file == null) throw new IOException("null target file");

        BackpackControlConfig partial;
        if (file.exists()) {
            partial = loadSingleFile(file);
            if (partial == null) partial = new BackpackControlConfig();
            if (partial.controlPoss == null) partial.controlPoss = new HashMap<>();
        } else {
            partial = new BackpackControlConfig();
            partial.controlPoss = new HashMap<>();
        }

        partial.controlPoss.put(key, values);
        writeJsonToFile(partial, file);

        if (this.controlPoss == null) this.controlPoss = new HashMap<>();
        this.controlPoss.put(key, values);
        if (sourceFiles == null) sourceFiles = new HashMap<>();
        sourceFiles.put(key, file);
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