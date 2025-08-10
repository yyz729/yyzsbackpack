package com.yyz.yyzsbackpack.item;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.resources.ResourceLocation;

public enum BackpackMaterials implements BackpackMaterial{

    IRON("iron",2,9, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/2x9backpack.png"),ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/iron_backpack.png")),
    GOLD("gold",4, 9, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/4x9backpack.png"), ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/gold_backpack.png")),
    DIAMOND("diamond",6, 9, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/6x9backpack.png"), ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/diamond_backpack.png")),
    NETHERITE("netherite",6, 9, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/6x9backpack.png"), ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/backpack/netherite_backpack.png"));

    private final String type;
    private final int columns;
    private final int rows;
    private final ResourceLocation guiTexture;
    private final ResourceLocation modelTexture;


    BackpackMaterials(String type, int columns, int rows, ResourceLocation guiTexture, ResourceLocation modelTexture) {
        this.type = type;
        this.columns = columns;
        this.rows = rows;
        this.guiTexture = guiTexture;
        this.modelTexture = modelTexture;
    }

    @Override
    public int getSize() {
        return columns * rows;
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return guiTexture;
    }

    @Override
    public ResourceLocation getModelTexture() {
        return modelTexture;
    }
}