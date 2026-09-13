package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveCToBackpackC2SPacket(MoveMode mode) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "movec_to_backpack");

    public static void write(FriendlyByteBuf buf, MoveCToBackpackC2SPacket packet) {
        buf.writeEnum(packet.mode());
    }

    public static MoveCToBackpackC2SPacket read(FriendlyByteBuf buf) {
        return new MoveCToBackpackC2SPacket(buf.readEnum(MoveMode.class));
    }
}