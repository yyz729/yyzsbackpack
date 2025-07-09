package com.yyz.yyzsbackpack.mixin.compat.accessories;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import io.wispforest.accessories.api.menu.AccessoriesBasedSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AccessoriesBasedSlot.class)
public abstract class AccessoriesBasedSlotMixin extends Slot {

    @Shadow(remap = false) @Final public LivingEntity entity;

    public AccessoriesBasedSlotMixin(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }



    @Override
    public void onTake(@NotNull Player player, ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BackpackItem && entity instanceof Player && !Backpack.getConfig().force_slot ) {
            if(!BackpackHelper.isModLoaded("trinkets")) {
                BackpackManager.saveBackpackContents(player.getInventory(), backpackStack, true);
            }
        }
        super.onTake(player, backpackStack);
    }

    @Override
    public void setByPlayer(@NotNull ItemStack newBackpackStack) {
        if(entity instanceof Player player && !Backpack.getConfig().force_slot ) {
            if(!BackpackHelper.isModLoaded("trinkets")) {
                ItemStack oldBackpackStack = this.getItem();
                if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.saveBackpackContents(player.getInventory(), oldBackpackStack, true);
                }

                super.setByPlayer(newBackpackStack);

                if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.restoreBackpackContents(player.getInventory(), newBackpackStack);
                }
                return;
            }
        }
        super.setByPlayer(newBackpackStack);
    }
}
