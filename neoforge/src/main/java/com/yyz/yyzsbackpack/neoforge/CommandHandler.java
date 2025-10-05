package com.yyz.yyzsbackpack.neoforge;

import com.yyz.yyzsbackpack.Backpack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Backpack.MOD_ID)
public class CommandHandler {
    @SubscribeEvent
    public static void onServerStarting(RegisterCommandsEvent event) {
        Backpack.registerCommands(event.getDispatcher());
    }

}