package com.yyz.yyzsbackpack.forge.mixin.compat.inventorio;

import com.yyz.yyzsbackpack.BackpackManager;
import de.rubixdev.inventorio.player.InventorioScreenHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventorioScreenHandler.class)
public abstract class InventorioScreenHandlerMixin extends AbstractContainerMenu{

    protected InventorioScreenHandlerMixin(@Nullable MenuType<?> arg, int i) {
        super(arg, i);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 39),remap = false)
    private int armorIndexChange(int og) {
        return og + 9 * 6 + 1;
    }


    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;)V", at = @At("RETURN"),remap = false)
    private void addSlots(int windowId, Inventory inventory, CallbackInfo ci) {
        BackpackManager.addBackpackSlots(this,inventory);
    }
}