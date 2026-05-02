package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yyz.yyzsbackpack.base.BackpackEffect;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpackConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 修改为存储多行注释的列表
    private static final Map<String, List<String>> FIELD_COMMENTS = new HashMap<>();
    static {
        addComment("quick_swap_backpack",
                "Allow swapping the currently equipped backpack by clicking a backpack item inside the backpack's own inventory",
                "If true, while the backpack GUI is open, clicking a backpack item in its internal slots will replace the one you are wearing"
        );
        addComment("use_dedicated_slot",
                "Force use of the mod's dedicated backpack slot instead of a baubles/trinkets accessory slot",
                "Only relevant when an accessory slot mod (e.g., Curios API) is present"
        );
        addComment("render_backpack_model",
                "Render the 3D backpack model on the player's back",
                "Disable if you don't need the visual model"
        );
        addComment("restrict_container_items",
                "Prevent container items (like Shulker Boxes) from being placed inside the backpack",
                "Helps avoid nested inventories"
        );
        addComment("slot_position_x", "X offset of the backpack equipment slot on the inventory screen (pixels)");
        addComment("slot_position_y", "Y offset of the backpack equipment slot on the inventory screen (pixels)");
        addComment("backpack_gui_x", "X offset of the backpack GUI window (pixels)");
        addComment("backpack_gui_y", "Y offset of the backpack GUI window (pixels)");

        addComment("restricted_items",
                "Additional items that cannot be placed into the backpack (besides container items if enabled above)",
                "Format: \"namespace:item_id\"",
                "Example: \"minecraft:diamond_sword\",",
                "         \"minecraft:tnt\""
        );

        // === 备份相关配置 ===
        addComment("backup_interval_seconds",
                "How often (in seconds) the backpack data is automatically saved as a backup",
                "Backups are stored in the same folder as the main data file"
        );
        addComment("max_backup_count",
                "Maximum number of backup files to keep per player",
                "When exceeded, the oldest backup is automatically deleted"
        );

        // === 负重相关配置 ===
        addComment("heavy_count",
                "Item count threshold that triggers the heavy effect",
                "If the total number of items in the backpack reaches or exceeds this value, the player will be slowed down"
        );
        addComment("heavy_slow_ratio",
                "Slowness multiplier when heavy",
                "Range: 0.0 (no slowdown) to 1.0 (extreme slowdown).",
                "Example: 0.6 means the player moves at 40% of normal speed"
        );
        addComment("heavy_jump_reduction_ratio",
                "Jump height reduction multiplier when heavy",
                "Range: 0.0 (no reduction) to 1.0 (cannot jump at all).",
                "Example: 0.5 means jump height is reduced by 50%"
        );
    }




    // 添加注释的辅助方法
    private static void addComment(String field, String... lines) {
        FIELD_COMMENTS.put(field, List.of(lines));
    }

    public boolean quick_swap_backpack = true;
    public boolean use_dedicated_slot = false;
    public boolean render_backpack_model = true;
    public boolean restrict_container_items = false;
    public int slot_position_x = 0;
    public int slot_position_y = 0;
    public int backpack_gui_x = 0;
    public int backpack_gui_y = 0;

    public int backup_interval_seconds = 30;
    public int max_backup_count = 5;

    public int heavy_count = 2;
    public double heavy_slow_ratio = 0.6;
    public double heavy_jump_reduction_ratio = 0.5;

    public Set<String> restricted_items = new HashSet<>();


    public static BackpackConfig loadConfig(File file) {
        BackpackConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                // 过滤注释行
                StringBuilder configBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // 跳过注释行和空行
                    if (line.trim().startsWith("//") || line.trim().isEmpty()) {
                        continue;
                    }
                    configBuilder.append(line).append("\n");
                }
                config = GSON.fromJson(configBuilder.toString(), BackpackConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            config = new BackpackConfig();
        }

        config.saveConfig(file);

        return config;
    }

    public void saveConfig(File configFile) {
        try {
            // 生成标准JSON
            String json = GSON.toJson(this);
            StringBuilder output = new StringBuilder();
            String[] lines = json.split("\n");

            // 正则匹配字段行
            Pattern fieldPattern = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:");
            for (String line : lines) {
                // 检查是否为字段行
                Matcher matcher = fieldPattern.matcher(line);
                if (matcher.find()) {
                    String fieldName = matcher.group(1);
                    // 添加多行注释
                    if (FIELD_COMMENTS.containsKey(fieldName)) {
                        String indent = line.substring(0, line.indexOf('"'));
                        for (String commentLine : FIELD_COMMENTS.get(fieldName)) {
                            output.append(indent).append("// ").append(commentLine).append("\n");
                        }
                    }
                }
                output.append(line).append("\n");
            }

            // 写入文件
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                writer.write(output.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }
}