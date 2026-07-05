package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ServerPacketHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SwitchBackpackC2SPacket.ID, (packet, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                Backpack.setSelectedIndex(player, packet.index());
                // 立即同步服务端额外槽位
                if (player.getInventory() instanceof IExtendedInventory extInv) {
                    extInv.yyzsbackpack$syncFromBackpack(Backpack.getSelectedBackpack(player));
                }
            });
        });
    }
}