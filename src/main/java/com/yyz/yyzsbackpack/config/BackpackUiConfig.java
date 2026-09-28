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

public class BackpackUiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile BackpackUiConfig INSTANCE;

    /**
     * 每个界面条目：List<int[]>，按分段顺序交替存放 [目标位置, 锚点百分比]：
     *   index 2i     -> 第 i 段锚点的目标位置（像素）[x, y]
     *   index 2i + 1 -> 第 i 段锚点在背包上的位置（%）[x%, y%]（0~100）
     *
     * 语义：把第 i 段上 (x%, y%) 那个点放到 (off[0], off[1]) 位置。
     *   [100, 0] （默认） = 该段右边缘对齐到 off 位置
     *   [0,   0]          = 该段左边缘对齐到 off 位置
     *   [100, 100]        = 该段右下角对齐到 off 位置
     *   [50,  50]         = 该段中心对齐到 off 位置
     *
     * 单段时等价于旧格式：{ [x, y], [anchorX, anchorY] }。
     */
    public Map<String, List<int[]>> uiOffsets = new HashMap<>() {{
        put("InventoryScreen",             Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("CraftingScreen",              Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("ContainerScreen",             Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("BlastFurnaceScreen",          Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("FurnaceScreen",               Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("SmokerScreen",                Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("BeaconScreen",                Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("BrewingStandScreen",          Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("CartographyTableScreen",      Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("CrafterScreen",               Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("DispenserScreen",             Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("EnchantmentScreen",           Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("GrindstoneScreen",            Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("HopperScreen",                Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("LoomScreen",                  Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("MerchantScreen",              Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("ShulkerBoxScreen",            Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("SmithingScreen",              Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("AnvilScreen",                 Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("StonecutterScreen",           Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("NautilusInventoryScreen",     Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("HorseInventoryScreen",        Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
        put("CreativeModeInventoryScreen", Arrays.asList(new int[]{0, 0}, new int[]{100, 0}));
    }};

    private transient Map<String, File> sourceFiles = new HashMap<>();
    private transient File defaultFile;
    private transient File configDir;

    public static BackpackUiConfig getInstance() {
        return INSTANCE;
    }

    // ============================================================
    // 分段访问：每段占 2 个条目（2i = 目标位置，2i + 1 = 锚点百分比）
    // ============================================================

    /** 指定分段的偏移（目标位置）。越界或缺失时回退到段 0。 */
    public static int[] offsetOf(List<int[]> list, int segmentIndex) {
        if (list == null || list.isEmpty()) return new int[]{0, 0};
        int idx = segmentIndex * 2;
        if (idx < 0 || idx >= list.size()) idx = 0;
        int[] v = list.get(idx);
        return (v != null && v.length >= 2) ? new int[]{v[0], v[1]} : new int[]{0, 0};
    }

    /** 指定分段的锚点百分比。越界或缺失时回退到段 0 的锚点。 */
    public static int[] anchorOf(List<int[]> list, int segmentIndex) {
        if (list == null || list.size() < 2) return new int[]{100, 0};
        int idx = segmentIndex * 2 + 1;
        if (idx < 0 || idx >= list.size()) idx = 1;
        int[] v = list.get(idx);
        if (v == null || v.length < 2) return new int[]{100, 0};
        return new int[]{
                Math.max(0, Math.min(100, v[0])),
                Math.max(0, Math.min(100, v[1]))
        };
    }

    /** 向后兼容：段 0 的偏移。 */
    public static int[] offsetOf(List<int[]> list) { return offsetOf(list, 0); }

    /** 向后兼容：段 0 的锚点。 */
    public static int[] anchorOf(List<int[]> list) { return anchorOf(list, 0); }

    // ============================================================
    // 生成 / 加载 / 保存
    // ============================================================

    public static void generateModDefaultConfig(String modId, Map<String, List<int[]>> defaultEntries) {
        Path uiDir = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/ui");
        File targetFile = uiDir.resolve(modId + ".json").toFile();

        if (!targetFile.exists()) {
            BackpackUiConfig partial = new BackpackUiConfig();
            partial.uiOffsets = new HashMap<>(defaultEntries);
            try {
                writeJsonToFile(partial, targetFile);
                Backpack.LOGGER.info("Generated default ui config for mod '{}' at {}", modId, targetFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate ui config for mod '{}'", modId, e);
            }
        }

        BackpackUiConfig inst = INSTANCE;
        if (inst != null && inst.uiOffsets != null) {
            for (Map.Entry<String, List<int[]>> e : defaultEntries.entrySet()) {
                if (!inst.uiOffsets.containsKey(e.getKey())) {
                    inst.uiOffsets.put(e.getKey(), e.getValue());
                    if (inst.sourceFiles != null) {
                        inst.sourceFiles.put(e.getKey(), targetFile);
                    }
                }
            }
        }
    }

    public static BackpackUiConfig loadConfig(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File defaultFile = new File(configDir, "default.json");
        if (!defaultFile.exists()) {
            try {
                writeJsonToFile(new BackpackUiConfig(), defaultFile);
                Backpack.LOGGER.info("Generated default ui config file at {}", defaultFile);
            } catch (IOException e) {
                Backpack.LOGGER.error("Failed to generate default ui config", e);
                BackpackUiConfig fallback = new BackpackUiConfig();
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
                    BackpackUiConfig partial = loadSingleFile(file);
                    if (partial != null && partial.uiOffsets != null) {
                        merged.putAll(partial.uiOffsets);
                        for (String key : partial.uiOffsets.keySet()) {
                            sourceFiles.put(key, file);
                        }
                    }
                } catch (Exception e) {
                    Backpack.LOGGER.error("Failed to load ui config from file: {}", file, e);
                }
            }
        }

        BackpackUiConfig config = new BackpackUiConfig();
        config.uiOffsets = merged;
        config.sourceFiles = sourceFiles;
        config.defaultFile = defaultFile;
        config.configDir = configDir;
        INSTANCE = config;
        return config;
    }

    public static BackpackUiConfig reload() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("yyzsbackpack/ui");
        return loadConfig(dir.toFile());
    }

    public Map<String, List<int[]>> getUiOffsets() {
        return this.uiOffsets;
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
            BackpackUiConfig partial = loadSingleFile(file);
            if (partial == null || partial.uiOffsets == null) return Collections.emptyList();
            List<String> keys = new ArrayList<>(partial.uiOffsets.keySet());
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
            BackpackUiConfig partial = loadSingleFile(file);
            if (partial == null || partial.uiOffsets == null) return copy;
            List<int[]> v = partial.uiOffsets.get(key);
            if (v == null) return copy;
            for (int[] a : v) copy.add(a.clone());
        } catch (IOException e) {
            Backpack.LOGGER.error("Failed to read value for {} from {}", key, file, e);
        }
        return copy;
    }

    public void saveKeyTo(String key, List<int[]> values, File file) throws IOException {
        if (file == null) throw new IOException("null target file");

        BackpackUiConfig partial;
        if (file.exists()) {
            partial = loadSingleFile(file);
            if (partial == null) partial = new BackpackUiConfig();
            if (partial.uiOffsets == null) partial.uiOffsets = new HashMap<>();
        } else {
            partial = new BackpackUiConfig();
            partial.uiOffsets = new HashMap<>();
        }

        partial.uiOffsets.put(key, values);
        writeJsonToFile(partial, file);

        if (this.uiOffsets == null) this.uiOffsets = new HashMap<>();
        this.uiOffsets.put(key, values);
        if (sourceFiles == null) sourceFiles = new HashMap<>();
        sourceFiles.put(key, file);
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