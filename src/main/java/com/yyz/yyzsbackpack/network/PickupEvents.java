package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.helper.BackpackInsert;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Backpack.MOD_ID)
public class PickupEvents {
    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        if (!PickupPrefs.prefers(player)) return;

        ItemEntity entity = event.getItemEntity();
        if (entity.hasPickUpDelay()) return;

        ItemStack backpack = BackpackSlotHelper.getSyncedBackpack(player);
        if (backpack.equals(ItemStack.EMPTY)) return;

        ItemStack stack = entity.getItem();
        int before = stack.getCount();
        ItemStack remainder = BackpackInsert.insert(player.getInventory(), stack);
        int moved = before - remainder.getCount();
        if (moved <= 0) return;

        player.take(entity, moved);
        player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), moved);

        if (remainder.isEmpty()) {
            entity.discard();
            event.setCanPickup(TriState.FALSE);
        } else {
            stack.setCount(remainder.getCount());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PickupPrefs.clear(event.getEntity().getUUID());
    }
}