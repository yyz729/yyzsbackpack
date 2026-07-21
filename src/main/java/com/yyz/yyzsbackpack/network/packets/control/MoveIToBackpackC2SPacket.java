package com.yyz.yyzsbackpack.network.packets.control;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MoveIToBackpackC2SPacket(boolean all) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "movei_to_backpack");

    public static void write(FriendlyByteBuf buf, MoveIToBackpackC2SPacket packet) {
        buf.writeBoolean(packet.all());
    }

    public static MoveIToBackpackC2SPacket read(FriendlyByteBuf buf) {
        return new MoveIToBackpackC2SPacket(buf.readBoolean());
    }
}