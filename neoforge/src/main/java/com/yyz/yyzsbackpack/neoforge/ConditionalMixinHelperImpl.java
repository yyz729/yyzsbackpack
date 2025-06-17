package com.yyz.yyzsbackpack.neoforge;


import net.neoforged.fml.loading.FMLLoader;

public class ConditionalMixinHelperImpl {
    public static boolean isModLoaded(String modId) {

        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }
}