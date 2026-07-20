package com.yyz.yyzsbackpack.api;

import net.minecraft.world.entity.player.Player;
import java.util.List;

/**
 * 提供玩家身上所有需要参与负重计算的槽位。
 */
public interface IWeightSlots {
    List<IBackpackSlot> getWeightSlots(Player player);
}