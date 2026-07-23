package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveBToInventoryC2SPacket(boolean all) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "moveb_to_inventory");

    public static void write(FriendlyByteBuf buf, MoveBToInventoryC2SPacket packet) {
        buf.writeBoolean(packet.all());
    }

    public static MoveBToInventoryC2SPacket read(FriendlyByteBuf buf) {
        return new MoveBToInventoryC2SPacket(buf.readBoolean());
    }
}