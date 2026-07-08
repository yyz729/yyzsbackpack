package com.yyz.yyzsbackpack.api;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface IBackpackSlots {
    List<IBackpackSlot> getSlots(Player player);
}