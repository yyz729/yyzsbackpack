package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record QuickMoveC2SPacket(int slotIndex) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "quick_move");

    public static void write(FriendlyByteBuf buf, QuickMoveC2SPacket packet) {
        buf.writeVarInt(packet.slotIndex());
    }

    public static QuickMoveC2SPacket read(FriendlyByteBuf buf) {
        return new QuickMoveC2SPacket(buf.readVarInt());
    }
}