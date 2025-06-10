package com.yyz.yyzsbackpack.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin  {

    @Redirect(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;size()I"))
    private int handleMouseReleased(DefaultedList<Slot> instance) {
        return 46;
    }
}