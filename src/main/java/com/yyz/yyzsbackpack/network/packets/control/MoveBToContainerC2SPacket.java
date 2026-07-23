package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveBToContainerC2SPacket(boolean all) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "moveb_to_container");

    public static void write(FriendlyByteBuf buf, MoveBToContainerC2SPacket packet) {
        buf.writeBoolean(packet.all());
    }

    public static MoveBToContainerC2SPacket read(FriendlyByteBuf buf) {
        return new MoveBToContainerC2SPacket(buf.readBoolean());
    }
}