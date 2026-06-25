package com.yyz.yyzsbackpack.api;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface BackpackSlotProvider {
    List<BackpackSlotReference> getSlots(Player player);
}