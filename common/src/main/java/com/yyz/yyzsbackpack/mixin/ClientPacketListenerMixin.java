package com.yyz.yyzsbackpack.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleContainerContent", at = @At("HEAD"), cancellable = true)
    private void onHandleContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) return;

        int slotCount = menu.slots.size();
        List<net.minecraft.world.item.ItemStack> items = packet.getItems();

        // 如果物品列表长度超过槽位数，进行裁剪后手动调用 initializeContents
        if (items.size() > slotCount) {
            // 创建裁剪后的子列表（0 到 slotCount-1）
            List<net.minecraft.world.item.ItemStack> trimmed = items.subList(0, slotCount);
            menu.initializeContents(packet.getStateId(), trimmed, packet.getCarriedItem());
            // 取消原方法，避免重复调用导致越界
            ci.cancel();
        }
    }
}