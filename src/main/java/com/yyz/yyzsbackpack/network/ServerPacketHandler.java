package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IPlayerBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ServerPacketHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SwitchBackpackC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackSlotHelper.setSelectedIndex(player, packet.index());

                // 立即同步服务端额外槽位
                if (player.getInventory() instanceof IExtendedInventory extInv) {
                    extInv.yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(player));
                }

                ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
                if (player instanceof IPlayerBackpackData data) {
                    data.yyzsbackpack$setSyncedBackpack(selected);
                }
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
            if (player instanceof IPlayerBackpackData data) {
                data.yyzsbackpack$setSyncedBackpack(selected);
            }
        });
    }
}