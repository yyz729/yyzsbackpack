package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.item.ModItems;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Backpack.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientBackpackEvents {

    @SubscribeEvent
    public static void onItemColor(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex == 0 && stack.getItem() instanceof DyeableLeatherItem dyeable) {
                return dyeable.hasCustomColor(stack) ? dyeable.getColor(stack) : -6265536;
            }
            return -1;
        }, ModItems.IRON_BACKPACK.get(),
           ModItems.GOLD_BACKPACK.get(),
           ModItems.DIAMOND_BACKPACK.get(),
           ModItems.NETHERITE_BACKPACK.get());
    }
}