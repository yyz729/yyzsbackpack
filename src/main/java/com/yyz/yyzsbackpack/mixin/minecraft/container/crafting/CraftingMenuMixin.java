package com.yyz.yyzsbackpack.mixin.minecraft.container.crafting;

import com.yyz.yyzsbackpack.api.BackpackContainerHelper;
import com.yyz.yyzsbackpack.api.IBackpackMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin implements IBackpackMenu {
    @Unique
    private int backpackSlotStart;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void onConstruct(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        BackpackContainerHelper.addBackpackSlotsIfPresent((AbstractCraftingMenu)(Object)this, inventory);
    }

    @Override
    public int yyzsbackpack$getBackpackSlotStart() {
        return backpackSlotStart;
    }

    @Override
    public void yyzsbackpack$setBackpackSlotStart(int start) {
        this.backpackSlotStart = start;
    }
}
