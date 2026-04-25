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
        addComment("quick_swap_backpack", "Enable quick backpack swapping");
        addComment("use_dedicated_slot", "Force use of dedicated backpack slot instead of accessory slot");
        addComment("render_backpack_model", "Render backpack model");
        addComment("restrict_container_items", "Prevent container items (like Shulker Boxes) from being placed in backpack");
        addComment("slot_position_x", "X position of backpack slot");
        addComment("slot_position_y", "Y position of backpack slot");
        addComment("backpack_gui_x", "X position of backpack GUI");
        addComment("backpack_gui_y", "Y position of backpack GUI");
        addComment("tooltip_modifier", "Backpack preview modifier key (alt, ctrl, none)");
        addComment("backpack_model_style", "detailed/simplified");

        // Multi-line comments
        addComment("restricted_items",
                "Additional container items",
                "Format: \"namespace:item_id\"",
                "Example: \"minecraft:shulker_box\",",
                "         \"minecraft:shulker_box\",",
                "         \"minecraft:shulker_box\","
        );

        // Detailed multi-line comment with example
        addComment("backpack_multi_effects",
                "Multi-tier backpack effects configuration",
                "Each element in the list corresponds to an additional backpack tier:",
                "  - Index 0: Effect for carrying 1 extra backpack",
                "  - Index 1: Effect for carrying 2 extra backpacks",
                "  - Index 2: Effect for carrying 3 extra backpacks",
                "  - And so on...",
                "",
                "Each effect must contain the following fields:",
                "  - \"effectType\": Effect ID (e.g. \"minecraft:speed\")",
                "  - \"amplifier\": Effect level (0-based)",
                "",
                "Example configuration:",
                "[",
                "  { // Effect for carrying 1 extra backpack",
                "    \"effectType\": \"none\", // No effect",
                "    \"amplifier\": 0",
                "  },",
                "  { // Effect for carrying 2 extra backpacks",
                "    \"effectType\": \"minecraft:slowness\",",
                "    \"amplifier\": 1",
                "  },",
                "  { // Effect for carrying 3 extra backpacks",
                "    \"effectType\": \"minecraft:slowness\",",
                "    \"amplifier\": 3",
                "  }",
                "]"
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
    public String tooltip_modifier = "shift";
    public Set<String> restricted_items = new HashSet<>();

    public List<BackpackEffect> backpack_multi_effects = new ArrayList<>() {{
        // 索引0: 携带1个额外背包的效果 (无效果)
        add(new BackpackEffect("none", 0));
        // 索引1: 携带2个额外背包的效果 (无效果)
        add(new BackpackEffect("none", 0));
        // 索引2: 携带2个额外背包的效果 (减速效果)
        add(new BackpackEffect("minecraft:slowness", 1));
    }};

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