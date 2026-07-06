package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IPlayerBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

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
//                    data.yyzsbackpack$setSyncedBackpackIndex(packet.index());
                }
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();

            Map<String, BackpackData> allData = BackpackDataLoader.getAllData();
            sender.sendPacket(new BackpackDataSyncS2CPacket(allData));

            ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
            int index = BackpackSlotHelper.getSelectedIndex(player);
            if (player instanceof IPlayerBackpackData data) {
                data.yyzsbackpack$setSyncedBackpack(selected);
//                data.yyzsbackpack$setSyncedBackpackIndex(index);
            }
        });
    }
}