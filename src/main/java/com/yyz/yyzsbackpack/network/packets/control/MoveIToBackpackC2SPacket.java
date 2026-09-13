package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveIToBackpackC2SPacket(MoveMode mode) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "movei_to_backpack");

    public static void write(FriendlyByteBuf buf, MoveIToBackpackC2SPacket packet) {
        buf.writeEnum(packet.mode());
    }

    public static MoveIToBackpackC2SPacket read(FriendlyByteBuf buf) {
        return new MoveIToBackpackC2SPacket(buf.readEnum(MoveMode.class));
    }
}