package com.yyz.yyzsbackpack.item;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.resources.ResourceLocation;

public enum BackpackMaterials implements BackpackMaterial{

    IRON("iron",2, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/2x9backpack")),
    GOLD("gold",4, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/4x9backpack")),
    DIAMOND("diamond",6, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/6x9backpack")),
    NETHERITE("netherite",6, ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID,"textures/gui/6x9backpack"));

    private final String type;
    private final int columns;
    private final ResourceLocation guiTexture;

    BackpackMaterials(String type, int columns, ResourceLocation guiTexture) {
        this.type = type;
        this.columns = columns;
        this.guiTexture = guiTexture;
    }

    public int getColumns() {
        return columns;
    }

    public String getType() {
        return type;
    }

    public ResourceLocation getBackpackGuiTexture(){
        return guiTexture;
    }

    public ResourceLocation getBackpackModelTexture(){
        return null;
    }
}