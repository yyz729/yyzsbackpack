package com.yyz.yyzsbackpack.forge;


import net.minecraftforge.fml.loading.FMLLoader;
import top.theillusivec4.curios.api.CuriosApi;

public class ConditionalMixinHelperImpl {
    public static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    public static int getCuriosSize(){
        return CuriosApi.getPlayerSlots().size();
    }
}