package com.yyz.yyzsbackpack.fabric.mixin.compat.reinforced;

import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler;
import atonkish.reinfcore.util.ReinforcingMaterial;
import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReinforcedStorageScreenHandler.class)
public abstract class ReinforcedStorageScreenHandlerMixin  extends AbstractContainerMenu {
    protected ReinforcedStorageScreenHandlerMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>", at = @At("RETURN"),remap = false)
    private void addSlots(MenuType<?> type, ReinforcingMaterial material, boolean isDoubleBlock, boolean isShulkerBox, int syncId, Inventory playerInventory, Container inventory, CallbackInfo ci) {
        SlotManager.addBackpackInventorySlots(this,playerInventory);
    }
}
