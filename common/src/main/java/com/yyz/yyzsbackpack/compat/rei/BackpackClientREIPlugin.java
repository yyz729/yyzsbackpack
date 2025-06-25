package com.yyz.yyzsbackpack.compat.rei;

import com.yyz.yyzsbackpack.api.BackpackExclusionZoneProvider;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Collections;
import java.util.stream.Collectors;

public class BackpackClientREIPlugin implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AbstractContainerScreen.class, screen -> {
            if (screen instanceof BackpackExclusionZoneProvider provider) {
                // 将 Rect2i 转换为 REI 的 Rectangle
                return provider.getBackpackExclusionZones().stream()
                        .map(rect -> new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }
}
