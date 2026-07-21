package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveCToInventoryC2SPacket(boolean all) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "movec_to_inventory");

    public static void write(FriendlyByteBuf buf, MoveCToInventoryC2SPacket packet) {
        buf.writeBoolean(packet.all());
    }

    public static MoveCToInventoryC2SPacket read(FriendlyByteBuf buf) {
        return new MoveCToInventoryC2SPacket(buf.readBoolean());
    }
}