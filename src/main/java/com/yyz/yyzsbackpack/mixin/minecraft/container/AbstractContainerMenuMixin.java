package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.yyz.yyzsbackpack.api.IBackpackMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IBackpackMenu {

    @Unique
    private int backpackSlotStart;

    @Override
    public int yyzsbackpack$getBackpackSlotStart() {
        return backpackSlotStart;
    }

    @Override
    public void yyzsbackpack$setBackpackSlotStart(int start) {
        this.backpackSlotStart = start;
    }
}