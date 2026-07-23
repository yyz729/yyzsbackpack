package com.yyz.yyzsbackpack.network.handler;

import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.api.util.SortAlgorithms;
import com.yyz.yyzsbackpack.network.packets.control.*;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ServerPacketHandler {

    public static void handleSwitchBackpack(SwitchBackpackC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            BackpackSlotHelper.setSelectedIndex(player, packet.index());
            if (player.getInventory() instanceof IExtendedInventory extInv) {
                extInv.yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(player));
            }
            ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
            if (player instanceof IBackpackData data) {
                data.yyzsbackpack$setSyncedBackpack(selected);
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleMoveIToBackpack(MoveIToBackpackC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            BackpackMenuHelper.moveIToBackpack(packet.all(), player);
        });
        context.setPacketHandled(true);
    }

    public static void handleMoveBToInventory(MoveBToInventoryC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            BackpackMenuHelper.moveBToInventory(packet.all(), player);
        });
        context.setPacketHandled(true);
    }

    public static void handleMoveCToInventory(MoveCToInventoryC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            BackpackMenuHelper.moveCToInventory(packet.all(), player);
        });
        context.setPacketHandled(true);
    }

    public static void handleMoveIToContainer(MoveIToContainerC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            BackpackMenuHelper.moveIToContainer(packet.all(), player);
        });
        context.setPacketHandled(true);
    }

    public static void handleMoveCToBackpack(MoveCToBackpackC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            BackpackMenuHelper.moveCToBackpack(packet.all(), player);
        });
        context.setPacketHandled(true);
    }

    public static void handleMoveBToContainer(MoveBToContainerC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            BackpackMenuHelper.moveBToContainer(packet.all(), player);
        });
        context.setPacketHandled(true);
    }

    public static void handleSortRequest(SortRequestC2SPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            SortAlgorithms.ensureInitialized(player.level().getServer());
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
        context.setPacketHandled(true);
    }
}