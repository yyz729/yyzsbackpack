package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.api.BackPackSlot;
import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin implements BackpackRenderCondition {



    @Shadow public abstract ItemStack getCarried();

    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract void setCarried(ItemStack stack);


    @Shadow public abstract Slot getSlot(int i);

    @Unique
    private boolean shouldRenderBackpack = false;

    @Override
    public boolean shouldRenderBackpack() {
        return this.shouldRenderBackpack;
    }

    @Override
    public void setRenderBackpack(boolean shouldRenderBackpack) {
        this.shouldRenderBackpack = shouldRenderBackpack;
    }

    @Unique
    private int backpackXOffset = 0;
    @Unique
    private int backpackYOffset = 0;

    @Unique
    private int equippackXOffset = 0;
    @Unique
    private int equippackYOffset = 0;

    @Override
    public int getBackpackXOffset() {
        return backpackXOffset;
    }

    @Override
    public int getBackpackYOffset() {
        return backpackYOffset;
    }

    @Override
    public void setBackpackOffset(int x, int y) {
        this.backpackXOffset = x;
        this.backpackYOffset = y;
    }

    @Override
    public int getEquippackXOffset() {
        return equippackXOffset;
    }

    @Override
    public int getEquippackYOffset() {
        return equippackYOffset;
    }

    @Override
    public void setEquippackOffset(int x, int y) {
        this.equippackXOffset = x;
        this.equippackYOffset = y;
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void handleBackpackSwap(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        if (slotIndex < 0 || actionType != ClickType.PICKUP || slots.get(slotIndex).getItem().getItem() instanceof BackpackItem) return;

        if(!(getSlot(slotIndex) instanceof BackPackSlot)) return;
        ItemStack back = player.getInventory().getItem(36).copy();
        ItemStack stack = getCarried().copy();
        if(!(back.getItem() instanceof BackpackItem) || !(stack.getItem() instanceof BackpackItem)) return;
        BackpackManager.saveBackpackContents(player.getInventory(), back);
        BackpackManager.restoreBackpackContents(player.getInventory(),stack);
        player.getInventory().setItem(36,stack);
        setCarried(back);
    }
}