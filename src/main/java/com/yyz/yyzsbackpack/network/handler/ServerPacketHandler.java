package com.yyz.yyzsbackpack.network.handler;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.api.util.SortAlgorithms;
import com.yyz.yyzsbackpack.network.packets.control.*;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 服务端网络包处理器（NeoForge 版）
 * 每个方法对应一个 C2S 包的处理逻辑
 */
public class ServerPacketHandler {

    public static void handleSwitchBackpack(SwitchBackpackC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
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
    }

    public static void handleMoveIToBackpack(MoveIToBackpackC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BackpackMenuHelper.moveIToBackpack(packet.all(), player);
        });
    }

    public static void handleMoveBToInventory(MoveBToInventoryC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BackpackMenuHelper.moveBToInventory(packet.all(), player);
        });
    }

    public static void handleMoveCToInventory(MoveCToInventoryC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BackpackMenuHelper.moveCToInventory(packet.all(), player);
        });
    }

    public static void handleMoveIToContainer(MoveIToContainerC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BackpackMenuHelper.moveIToContainer(packet.all(), player);
        });
    }

    public static void handleMoveCToBackpack(MoveCToBackpackC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BackpackMenuHelper.moveCToBackpack(packet.all(), player);
        });
    }

    public static void handleMoveBToContainer(MoveBToContainerC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            BackpackMenuHelper.moveBToContainer(packet.all(), player);
        });
    }

    public static void handleSortRequest(SortRequestC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            SortAlgorithms.ensureInitialized(player.level().getServer());
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
    }
}