package com.yyz.yyzsbackpack.mixin.minecraft.accessor;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface MenuAccessor {

    @Invoker("addSlot")
    Slot invokeAddSlot(Slot slot);
}