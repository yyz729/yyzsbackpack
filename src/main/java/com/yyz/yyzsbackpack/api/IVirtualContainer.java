package com.yyz.yyzsbackpack.api;

import com.yyz.yyzsbackpack.api.enums.MoveMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public interface IVirtualContainer {
    /**
     * 是否匹配当前菜单
     */
    boolean matches(AbstractContainerMenu menu);

    /**
     * 执行转移
     *
     * @param menu          当前菜单
     * @param player        服务器玩家
     * @param sourceStart   源范围起始（玩家物品栏或背包）
     * @param sourceEnd     源范围结束
     * @param targetStart   目标范围起始（玩家物品栏或背包）
     * @param targetEnd     目标范围结束
     * @param toContainer   true = 玩家侧 → 虚拟容器；false = 虚拟容器 → 玩家侧
     * @param all           true = 全部移动；false = 只移动匹配类型
     * @param matchTypes    匹配类型列表（all=false 时使用，可为空）
     * @return 是否发生了任何移动
     */
    boolean transfer(AbstractContainerMenu menu,
                     ServerPlayer player,
                     int sourceStart, int sourceEnd,
                     int targetStart, int targetEnd,
                     boolean toContainer,
                     MoveMode mode,
                     List<ItemStack> matchTypes);

    default List<ItemStack> getStoredItemTypes(AbstractContainerMenu menu) {
        return Collections.emptyList();
    }
}