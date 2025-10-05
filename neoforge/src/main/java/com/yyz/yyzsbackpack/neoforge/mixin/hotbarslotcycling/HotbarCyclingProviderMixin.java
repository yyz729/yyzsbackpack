package com.yyz.yyzsbackpack.neoforge.mixin.hotbarslotcycling;

import fuzs.hotbarslotcycling.api.v1.client.HotbarCyclingProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HotbarCyclingProvider.class)
public class HotbarCyclingProviderMixin {
    @Redirect(method = "getFilledSlot(IZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I"))
    private int handleMouseReleased(NonNullList<Slot> instance) {
        return 36;
    }
}
