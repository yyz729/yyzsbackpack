package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveIToContainerC2SPacket(boolean all) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "movei_to_container");

    public static void write(FriendlyByteBuf buf, MoveIToContainerC2SPacket packet) {
        buf.writeBoolean(packet.all());
    }

    public static MoveIToContainerC2SPacket read(FriendlyByteBuf buf) {
        return new MoveIToContainerC2SPacket(buf.readBoolean());
    }
}