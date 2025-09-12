package com.yyz.yyzsbackpack.fabric.data;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.data.BackpackMaterialManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class BackpackMaterialManagerFabric extends BackpackMaterialManager implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "backpack_materials");
    }
}
