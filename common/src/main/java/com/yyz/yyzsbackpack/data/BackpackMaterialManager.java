// BackpackMaterialManager.java
package com.yyz.yyzsbackpack.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BackpackMaterialManager extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, BackpackMaterial> MATERIALS = new HashMap<>();

    public BackpackMaterialManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("backpack_materials"));
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
    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resources,
                        @NotNull ResourceManager resourceManager, 
                        @NotNull ProfilerFiller profiler) {
        MATERIALS.clear();
        
        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
//            try {
                ResourceLocation location = entry.getKey();
                JsonElement element = entry.getValue();
                
                BackpackMaterialData data = GSON.fromJson(element, BackpackMaterialData.class);
                MATERIALS.put(data.type, new DataDrivenBackpackMaterial(data));

//            } catch (Exception e) {
//
//            }
        }
        
        // 添加默认材质，如果JSON文件中没有定义
//        addDefaultMaterials();
        

    }
    
    private void addDefaultMaterials() {
        // 只有在没有自定义材质时才添加默认材质
        if (MATERIALS.isEmpty()) {
            MATERIALS.put("iron", new DataDrivenBackpackMaterial(
                new BackpackMaterialData("iron", 18, 2, 9, 50, 174,
                    Backpack.MOD_ID + ":textures/gui/2x9backpack.png",
                    Backpack.MOD_ID + ":textures/backpack/iron_backpack.png")
            ));
            
            MATERIALS.put("gold", new DataDrivenBackpackMaterial(
                new BackpackMaterialData("gold", 36, 4, 9, 86, 174,
                    Backpack.MOD_ID + ":textures/gui/4x9backpack.png",
                    Backpack.MOD_ID + ":textures/backpack/gold_backpack.png")
            ));
            
            MATERIALS.put("diamond", new DataDrivenBackpackMaterial(
                new BackpackMaterialData("diamond", 54, 6, 9, 122, 174,
                    Backpack.MOD_ID + ":textures/gui/6x9backpack.png",
                    Backpack.MOD_ID + ":textures/backpack/diamond_backpack.png")
            ));
            
            MATERIALS.put("netherite", new DataDrivenBackpackMaterial(
                new BackpackMaterialData("netherite", 54, 6, 9, 122, 174,
                    Backpack.MOD_ID + ":textures/gui/6x9backpack.png",
                    Backpack.MOD_ID + ":textures/backpack/netherite_backpack.png")
            ));
        }
    }
}