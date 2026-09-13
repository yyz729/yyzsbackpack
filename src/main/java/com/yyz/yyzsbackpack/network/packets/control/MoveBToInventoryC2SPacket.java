package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveBToInventoryC2SPacket(MoveMode mode) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "moveb_to_inventory");

    public static void write(FriendlyByteBuf buf, MoveBToInventoryC2SPacket packet) {
        buf.writeEnum(packet.mode());
    }

    public static MoveBToInventoryC2SPacket read(FriendlyByteBuf buf) {
        return new MoveBToInventoryC2SPacket(buf.readEnum(MoveMode.class));
    }
}