// DataDrivenBackpackMaterial.java
package com.yyz.yyzsbackpack.data;

import com.yyz.yyzsbackpack.item.BackpackMaterial;
import net.minecraft.resources.ResourceLocation;

public class DataDrivenBackpackMaterial implements BackpackMaterial {
    private final BackpackMaterialData data;
    
    public DataDrivenBackpackMaterial(BackpackMaterialData data) {
        this.data = data;
    }
    
    @Override
    public int getSize() {
        return data.size;
    }
    
    @Override
    public int getColumns() {
        return data.columns;
    }
    
    @Override
    public int getRows() {
        return data.rows;
    }
    
    @Override
    public String getType() {
        return data.type;
    }
    
    @Override
    public int guiWidth() {
        return data.width;
    }
    
    @Override
    public int guiHeight() {
        return data.height;
    }
    
    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.parse(data.guiTexture);
    }
    
    @Override
    public ResourceLocation getModelTexture() {
        return ResourceLocation.parse(data.modelTexture);
    }
}