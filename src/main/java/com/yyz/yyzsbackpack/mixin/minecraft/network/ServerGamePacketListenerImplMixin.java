package com.yyz.yyzsbackpack.mixin.minecraft.network;

import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"), cancellable = true)
    private void yyzsbackpack$acceptBackpackCreativeSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (!this.player.serverLevel().getServer().isSameThread()) {
            return;
        }
        if (!this.player.gameMode.isCreative()) {
            return;
        }

        int slotNum = packet.slotNum();
        AbstractContainerMenu menu = this.player.inventoryMenu;
        if (slotNum < 0 || slotNum >= menu.slots.size()) {
            return;
        }
        Slot slot = menu.getSlot(slotNum);
        if (!(slot instanceof BackpackSlot)) {
            return;
        }

        ItemStack stack = packet.itemStack();
        if (!stack.isItemEnabled(this.player.level().enabledFeatures())) {
            ci.cancel();
            return;
        }
        if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
            ci.cancel();
            return;
        }

        slot.setByPlayer(stack);
        menu.broadcastChanges();
        ci.cancel();
    }
}
