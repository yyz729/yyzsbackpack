package com.yyz.yyzsbackpack.forge;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Backpack.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandHandler {
    @SubscribeEvent
    public static void onServerStarting(RegisterCommandsEvent event) {
        Backpack.registerCommands(event.getDispatcher());
    }

}