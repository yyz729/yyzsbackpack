package com.yyz.yyzsbackpack.fabric.mixin.compat.itemindicator;

import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import de.guntram.mcmod.durabilityviewer.itemindicator.InventorySlotsIndicator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventorySlotsIndicator.class)
public class InventorySlotsIndicatorMixin {

    @Mutable
    @Shadow(remap = false) @Final ItemStack stack;

    @Mutable
    @Shadow(remap = false) @Final int emptySlots;

    @Inject(method = "<init>", at = @At("RETURN"),remap = false)
    private void injected(Inventory inventory, CallbackInfo ci) {
        // 取消原构造函数的执行
        // 创建箱子物品显示
        this.stack = new ItemStack(Blocks.CHEST);
        int slots = 0;

        // 获取实际有效的背包大小
        int effectiveSize = getEffectiveInventorySize(inventory)+1;

        // 只遍历有效槽位范围
        for (int i = 0; i < effectiveSize; i++) {
            ItemStack itemStack = inventory.items.get(i);
            if (itemStack.isEmpty() && i != 36+ BackpackHelper.getMaxBackpackSize()) {
                slots++;
            }
        }

        this.emptySlots = slots;
    }


    /**
     * 计算实际有效的背包槽位数量
     * 基础槽位 (36) + 背包扩展槽位
     */
    @Unique
    private int getEffectiveInventorySize(Inventory inventory) {
        // 获取玩家当前装备的背包
        ItemStack backpackStack = BackpackPlatform.getEquipped(inventory.player);

        // 如果有背包且是自定义背包类型
        if (!backpackStack.isEmpty() && backpackStack.getItem() instanceof BackpackItem backpackItem) {

            return 36 + backpackItem.getBackpackType().getSize();
        }

        // 没有背包时只计算基础槽位
        return 36;
    }
}
