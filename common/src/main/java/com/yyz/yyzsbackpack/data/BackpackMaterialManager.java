// BackpackMaterialManager.java
package com.yyz.yyzsbackpack.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackMaterial;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BackpackMaterialManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, BackpackMaterial> MATERIALS = new HashMap<>();

    public static void loadMaterials() {
        MATERIALS.clear();

        Path materialsDir = BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack/materials");
        
        // 确保目录存在
        try {
            Files.createDirectories(materialsDir);
        } catch (IOException e) {
            System.err.println("Failed to create materials directory: " + e.getMessage());
            return;
        }
        
        // 加载默认材料配置（如果不存在）
        createDefaultMaterials(materialsDir);
        
        // 加载所有材料文件
        try {
            Files.list(materialsDir)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(BackpackMaterialManager::loadMaterialFile);
        } catch (IOException e) {
            System.err.println("Failed to read materials directory: " + e.getMessage());
        }
    }
    
    private static void loadMaterialFile(Path filePath) {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            
            BackpackMaterialData data = GSON.fromJson(reader, BackpackMaterialData.class);
            if (data != null && data.type != null) {
                MATERIALS.put(data.type, new DataDrivenBackpackMaterial(data));
                System.out.println("Loaded backpack material: " + data.type + " (" + data.columns + "x" + data.rows + ")");
            }
        } catch (IOException e) {
            System.err.println("Failed to load material file: " + filePath.getFileName() + " - " + e.getMessage());
        }
    }
    
    private static void createDefaultMaterials(Path materialsDir) {
        // 创建铁背包材料
        createDefaultMaterial(materialsDir, "iron.json",
                "{\n" +
                        "  \"type\": \"iron\",\n" +
                        "  \"size\": 18,\n" +
                        "  \"columns\": 2,\n" +
                        "  \"rows\": 9,\n" +
                        "  \"guiWidth\": 50,\n" +
                        "  \"guiHeight\": 174,\n" +
                        "  \"guiTexture\": \"yyzsbackpack:textures/gui/2x9backpack.png\",\n" +
                        "  \"modelTexture\": \"yyzsbackpack:textures/backpack/iron_backpack.png\"\n" +
                        "}");

        // 创建金背包材料
        createDefaultMaterial(materialsDir, "gold.json",
                "{\n" +
                        "  \"type\": \"gold\",\n" +
                        "  \"size\": 36,\n" +
                        "  \"columns\": 4,\n" +
                        "  \"rows\": 9,\n" +
                        "  \"guiWidth\": 86,\n" +
                        "  \"guiHeight\": 174,\n" +
                        "  \"guiTexture\": \"yyzsbackpack:textures/gui/4x9backpack.png\",\n" +
                        "  \"modelTexture\": \"yyzsbackpack:textures/backpack/gold_backpack.png\"\n" +
                        "}");

        // 创建钻石背包材料
        createDefaultMaterial(materialsDir, "diamond.json",
                "{\n" +
                        "  \"type\": \"diamond\",\n" +
                        "  \"size\": 54,\n" +
                        "  \"columns\": 6,\n" +
                        "  \"rows\": 9,\n" +
                        "  \"guiWidth\": 122,\n" +
                        "  \"guiHeight\": 174,\n" +
                        "  \"guiTexture\": \"yyzsbackpack:textures/gui/6x9backpack.png\",\n" +
                        "  \"modelTexture\": \"yyzsbackpack:textures/backpack/diamond_backpack.png\"\n" +
                        "}");

        // 创建下界合金背包材料
        createDefaultMaterial(materialsDir, "netherite.json",
                "{\n" +
                        "  \"type\": \"netherite\",\n" +
                        "  \"size\": 54,\n" +
                        "  \"columns\": 6,\n" +
                        "  \"rows\": 9,\n" +
                        "  \"guiWidth\": 122,\n" +
                        "  \"guiHeight\": 174,\n" +
                        "  \"guiTexture\": \"yyzsbackpack:textures/gui/6x9backpack.png\",\n" +
                        "  \"modelTexture\": \"yyzsbackpack:textures/backpack/netherite_backpack.png\"\n" +
                        "}");
    }
    
    private static void createDefaultMaterial(Path materialsDir, String fileName, String content) {
        Path filePath = materialsDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            try {
                Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("Failed to create default material file: " + fileName + " - " + e.getMessage());
            }
        }
    }
    
    public static BackpackMaterial getMaterial(String type) {
        return MATERIALS.get(type);
    }
    
    public static Map<String, BackpackMaterial> getMaterials() {
        return MATERIALS;
    }

    public static int getMaxSize() {
        int maxSize = 0;
        for (BackpackMaterial material : MATERIALS.values()) {
            if (material.getSize() > maxSize) {
                maxSize = material.getSize();
            }
        }
        return maxSize;
    }
}