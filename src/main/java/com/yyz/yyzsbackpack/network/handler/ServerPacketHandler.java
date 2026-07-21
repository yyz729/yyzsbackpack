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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ServerPacketHandler {
    public static void register() {
        // SwitchBackpack
        ServerPlayNetworking.registerGlobalReceiver(
                SwitchBackpackC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    SwitchBackpackC2SPacket packet = SwitchBackpackC2SPacket.read(buf);
                    server.execute(() -> {
                        BackpackSlotHelper.setSelectedIndex(player, packet.index());

                        if (player.getInventory() instanceof IExtendedInventory extInv) {
                            extInv.yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(player));
                        }

                        ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
                        if (player instanceof IBackpackData data) {
                            data.yyzsbackpack$setSyncedBackpack(selected);
                        }
                    });
                }
        );

        // MoveIToBackpack
        ServerPlayNetworking.registerGlobalReceiver(
                MoveIToBackpackC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    MoveIToBackpackC2SPacket packet = MoveIToBackpackC2SPacket.read(buf);
                    server.execute(() -> BackpackMenuHelper.moveIToBackpack(packet.all(), player));
                }
        );

        // MoveBToInventory
        ServerPlayNetworking.registerGlobalReceiver(
                MoveBToInventoryC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    MoveBToInventoryC2SPacket packet = MoveBToInventoryC2SPacket.read(buf);
                    server.execute(() -> BackpackMenuHelper.moveBToInventory(packet.all(), player));
                }
        );

        // MoveCToInventory
        ServerPlayNetworking.registerGlobalReceiver(
                MoveCToInventoryC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    MoveCToInventoryC2SPacket packet = MoveCToInventoryC2SPacket.read(buf);
                    server.execute(() -> BackpackMenuHelper.moveCToInventory(packet.all(), player));
                }
        );

        // MoveIToContainer
        ServerPlayNetworking.registerGlobalReceiver(
                MoveIToContainerC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    MoveIToContainerC2SPacket packet = MoveIToContainerC2SPacket.read(buf);
                    server.execute(() -> BackpackMenuHelper.moveIToContainer(packet.all(), player));
                }
        );

        // MoveCToBackpack
        ServerPlayNetworking.registerGlobalReceiver(
                MoveCToBackpackC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    MoveCToBackpackC2SPacket packet = MoveCToBackpackC2SPacket.read(buf);
                    server.execute(() -> BackpackMenuHelper.moveCToBackpack(packet.all(), player));
                }
        );

        // MoveBToContainer
        ServerPlayNetworking.registerGlobalReceiver(
                MoveBToContainerC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    MoveBToContainerC2SPacket packet = MoveBToContainerC2SPacket.read(buf);
                    server.execute(() -> BackpackMenuHelper.moveBToContainer(packet.all(), player));
                }
        );

        // SortRequest
        ServerPlayNetworking.registerGlobalReceiver(
                SortRequestC2SPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    SortRequestC2SPacket packet = SortRequestC2SPacket.read(buf);
                    server.execute(() -> {
                        SortAlgorithms.ensureInitialized(server);
                        AbstractContainerMenu menu = player.containerMenu;
                        Comparator<ItemStack> comparator = SortAlgorithms.getComparator(packet.algorithmId());
                        int mask = packet.targetMask();

                        List<int[]> ranges = new ArrayList<>();

                        if ((mask & 1) != 0) {
                            int start = BackpackMenuHelper.getBackpackSlotStart(menu);
                            int size = BackpackSlotHelper.getBackpackSize(player);
                            if (start >= 0 && size > 0) {
                                int end = Math.min(start + size, menu.slots.size());
                                ranges.add(new int[]{start, end});
                            }
                        }

                        if ((mask & 2) != 0) {
                            int[] invRange = BackpackMenuHelper.findInventoryMainRange(menu, player);
                            if (invRange != null) ranges.add(invRange);
                        }

                        if ((mask & 4) != 0) {
                            int[] containerRange = BackpackMenuHelper.findContainerRange(menu, player);
                            if (containerRange != null) ranges.add(containerRange);
                        }

                        if (ranges.isEmpty()) return;

                        for (int[] range : ranges) {
                            BackpackMenuHelper.sortSlots(menu, range[0], range[1], comparator);
                        }
                    });
                }
        );

        // 玩家加入时同步背包数据
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            Map<String, BackpackData> allData = BackpackDataLoader.getAllData();

            FriendlyByteBuf buf = PacketByteBufs.create();
            BackpackDataSyncS2CPacket.write(buf, new BackpackDataSyncS2CPacket(allData));
            ServerPlayNetworking.send(player, BackpackDataSyncS2CPacket.ID, buf);

            ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
            if (player instanceof IBackpackData data) {
                data.yyzsbackpack$setSyncedBackpack(selected);
            }
        });
    }
}