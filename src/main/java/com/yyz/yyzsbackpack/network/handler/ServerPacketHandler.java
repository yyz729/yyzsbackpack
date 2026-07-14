package com.yyz.yyzsbackpack.network.handler;

import com.yyz.yyzsbackpack.api.util.SortAlgorithms;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import com.yyz.yyzsbackpack.network.packets.data.BackpackDataSyncS2CPacket;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import com.yyz.yyzsbackpack.network.packets.control.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
                }
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();

            Map<String, BackpackData> allData = BackpackDataLoader.getAllData();
            sender.sendPacket(new BackpackDataSyncS2CPacket(allData));

            ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
            if (player instanceof IBackpackData data) {
                data.yyzsbackpack$setSyncedBackpack(selected);
            }
        });

        // 接收 MoveB 请求
        ServerPlayNetworking.registerGlobalReceiver(MoveIToBackpackC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveIToBackpack(packet.all(), player);
            });
        });

        // 接收 MoveI 请求
        ServerPlayNetworking.registerGlobalReceiver(MoveBToInventoryC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveBToInventory(packet.all(), player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(MoveCToInventoryC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveCToInventory(packet.all(), player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(MoveIToContainerC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveIToContainer(packet.all(), player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(MoveCToBackpackC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveCToBackpack(packet.all(), player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(MoveBToContainerC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BackpackMenuHelper.moveBToContainer(packet.all(), player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SortRequestC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                SortAlgorithms.ensureInitialized(context.server());
                AbstractContainerMenu menu = player.containerMenu;
                Comparator<ItemStack> comparator = SortAlgorithms.getComparator(packet.algorithmId());
                int mask = packet.targetMask();

                List<int[]> ranges = new ArrayList<>();

                // 背包
                if ((mask & 1) != 0) {
                    int start = BackpackMenuHelper.getBackpackSlotStart(menu);
                    int size = BackpackSlotHelper.getBackpackSize(player);
                    if (start >= 0 && size > 0) {
                        int end = Math.min(start + size, menu.slots.size());
                        ranges.add(new int[]{start, end});
                    }
                }

                // 物品栏（快捷栏 + 主物品栏）
                if ((mask & 2) != 0) {
                    int[] invRange = BackpackMenuHelper.findInventoryMainRange(menu, player);
                    if (invRange != null) ranges.add(invRange);
                }

                // 容器
                if ((mask & 4) != 0) {
                    int[] containerRange = BackpackMenuHelper.findContainerRange(menu, player);
                    if (containerRange != null) ranges.add(containerRange);
                }

                if (ranges.isEmpty()) return;

                for (int[] range : ranges) {
                    BackpackMenuHelper.sortSlots(menu, range[0], range[1], comparator);
                }
            });
        });
    }
}