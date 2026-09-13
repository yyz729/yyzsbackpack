package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveCToInventoryC2SPacket(MoveMode mode) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "movec_to_inventory");

    public static void write(FriendlyByteBuf buf, MoveCToInventoryC2SPacket packet) {
        buf.writeEnum(packet.mode());
    }

    public static MoveCToInventoryC2SPacket read(FriendlyByteBuf buf) {
        return new MoveCToInventoryC2SPacket(buf.readEnum(MoveMode.class));
    }
}