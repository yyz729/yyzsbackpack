package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SortRequestC2SPacket(int algorithmId, int targetMask) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "sort_request");

    public static void write(FriendlyByteBuf buf, SortRequestC2SPacket packet) {
        buf.writeInt(packet.algorithmId());
        buf.writeInt(packet.targetMask());
    }

    public static SortRequestC2SPacket read(FriendlyByteBuf buf) {
        return new SortRequestC2SPacket(buf.readInt(), buf.readInt());
    }
}