package com.yyz.yyzsbackpack.network.packets.data;

import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SwitchBackpackC2SPacket(int index) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "switch_backpack");

    public static void write(FriendlyByteBuf buf, SwitchBackpackC2SPacket packet) {
        buf.writeInt(packet.index());
    }

    public static SwitchBackpackC2SPacket read(FriendlyByteBuf buf) {
        return new SwitchBackpackC2SPacket(buf.readInt());
    }
}