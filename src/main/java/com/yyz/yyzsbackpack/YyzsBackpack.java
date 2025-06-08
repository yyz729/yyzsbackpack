package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.item.ModItems;
import net.fabricmc.api.ModInitializer;


public class YyzsBackpack implements ModInitializer {

    public static final String MOD_ID = "yyzsbackpack";

    @Override
    public void onInitialize() {
        ModItems.register();
    }



}
