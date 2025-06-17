package com.yyz.yyzsbackpack.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class ConditionalMixinHelperImpl {
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}