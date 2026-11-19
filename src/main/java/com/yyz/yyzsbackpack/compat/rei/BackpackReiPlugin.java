package com.yyz.yyzsbackpack.compat.rei;

import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;

public class BackpackReiPlugin implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AbstractContainerScreen.class, screen -> {
            java.awt.Rectangle bounds = BackpackScreenHelper.getBackpackBackgroundBounds(screen);
            if (bounds == null) {
                return List.of();
            }
            return List.of(new Rectangle(bounds.x - 4, bounds.y - 4, bounds.width + 12, bounds.height + 8));
        });
    }
}
