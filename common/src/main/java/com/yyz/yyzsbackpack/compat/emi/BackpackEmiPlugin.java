package com.yyz.yyzsbackpack.compat.emi;

import com.yyz.yyzsbackpack.base.BackpackExclusionZoneProvider;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

@EmiEntrypoint
public class BackpackEmiPlugin implements EmiPlugin {

    @Override
    public void initialize(EmiInitRegistry registry) {
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            if (screen instanceof AbstractContainerScreen<?> handledScreen) {
                if (handledScreen instanceof BackpackExclusionZoneProvider provider) {
                    for (Rect2i zone : provider.getBackpackExclusionZones()) {
                        consumer.accept(new Bounds(
                                zone.getX(),
                                zone.getY(),
                                zone.getWidth(),
                                zone.getHeight()
                        ));
                    }
                }
            }
        });
    }

}