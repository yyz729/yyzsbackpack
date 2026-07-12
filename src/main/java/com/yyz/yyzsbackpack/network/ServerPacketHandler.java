package com.yyz.yyzsbackpack.network;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
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
                if (player instanceof IBackpackData data) {
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
            if (player instanceof IBackpackData data) {
                data.yyzsbackpack$setSyncedBackpack(selected);
//                data.yyzsbackpack$setSyncedBackpackIndex(index);
            }
        });

        // 接收 MoveB 请求
        ServerPlayNetworking.registerGlobalReceiver(MoveBToBackpackC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveIToBackpack(packet.all(), player);
            });
        });

        // 接收 MoveI 请求
        ServerPlayNetworking.registerGlobalReceiver(MoveIToInventoryC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveBToInventory(packet.all(), player);
            });
        });
    }
}