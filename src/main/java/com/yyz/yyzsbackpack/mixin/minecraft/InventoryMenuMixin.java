package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.api.inventory.IBackpackMenu;
import com.yyz.yyzsbackpack.api.inventory.IExtendedInventory;
import com.yyz.yyzsbackpack.container.BackpackSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractCraftingMenu implements IBackpackMenu {

    @Shadow
    @Final
    private Player owner;

    @Shadow
    protected abstract @NonNull Player owner();

    @Unique
    private int backpackSlotStart;

    public InventoryMenuMixin(MenuType<?> menuType, int containerId, int width, int height) {
        super(menuType, containerId, width, height);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        backpackSlotStart = this.slots.size();
        for (int i = 0; i < 256; i++) {
            this.addSlot(new BackpackSlot(inventory, backpackSlotStart + i, -1000, -1000, backpackSlotStart));
        }
    }

    @Override
    public int yyzsbackpack$getBackpackSlotStart() {
        return backpackSlotStart;
    }

    @Unique
    private static ItemStack getEquippedBackpack(Player player) {
        ItemStack mainHand = player.getItemBySlot(EquipmentSlot.CHEST);
        if (mainHand.getItem() instanceof BackpackItem) {
            return mainHand;
        }
        return ItemStack.EMPTY;
    }
}
