package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackEffect;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BackpackStorage {
    // 保存背包内容到数据组件
    public static void saveBackpackContents(Container inventory, ItemStack backpackStack, boolean b) {
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
//        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = backpackItem.getBackpackType().getSize();
        //columns * 9;

        // 创建固定大小的列表（所有槽位，包括空）
        List<ItemStack> items = new ArrayList<>(numSlots);
        for (int i = 0; i < numSlots; i++) {
            int slotIndex = 36 + i;
            ItemStack stack = inventory.getItem(slotIndex);
            // 复制堆栈防止引用问题
            items.add(stack.copy());
            // 清空原库存槽位
            if(b) {
                inventory.setItem(slotIndex, ItemStack.EMPTY);
            }
        }

        // 设置数据组件
        backpackStack.set(BackpackPlatform.getBackpackItemsComponent(), items);
    }

    // 从数据组件恢复背包内容
    public static void restoreBackpackContents(Container inventory, ItemStack backpackStack) {
        // 获取数据组件

        List<ItemStack> items = backpackStack.get(BackpackPlatform.getBackpackItemsComponent());
        if (items == null) return;

        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
//        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = backpackItem.getBackpackType().getSize();
        //columns * 9;

        // 恢复物品到对应槽位
        for (int i = 0; i < Math.min(items.size(), numSlots); i++) {
            ItemStack stack = items.get(i);
            // 只恢复非空堆栈
            if (!stack.isEmpty()) {
                inventory.setItem(36 + i, stack.copy());
            }
        }

        // 移除数据组件
        backpackStack.remove(BackpackPlatform.getBackpackItemsComponent());
    }


    public static int countNonEmptyBackpacks(Container playerInventory) {
        int nonEmptyBackpackCount = 0;

        // 遍历玩家物品栏所有槽位
        for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
            ItemStack stack = playerInventory.getItem(slot);

            // 检查是否是背包物品
            if (stack.getItem() instanceof BackpackItem) {
                // 获取背包的存储数据组件
                List<ItemStack> backpackItems = stack.get(BackpackPlatform.getBackpackItemsComponent());

                // 检查背包是否有存储内容
                if (backpackItems != null) {
                    // 遍历背包内所有物品槽位
                    for (ItemStack content : backpackItems) {
                        if (!content.isEmpty()) {
                            nonEmptyBackpackCount++; // 发现非空物品，计数+1
                            break; // 跳出内部循环，继续检查下一个背包
                        }
                    }
                }
            }
        }
        return nonEmptyBackpackCount;
    }



    public static void updateEffectsByBackpackCount(Player player, List<Holder<MobEffect>> lastAppliedEffects) {
        int backpackCount = BackpackStorage.countNonEmptyBackpacks(player.getInventory());

        // 移除不再需要的老效果
        for (Holder<MobEffect> effect : new ArrayList<>(lastAppliedEffects)) {
            player.removeEffect(effect);
            lastAppliedEffects.remove(effect);
        }

        // 检查条件并应用新效果
        List<BackpackEffect> effects = Backpack.getConfig().backpack_multi_effects;
        if (backpackCount > 0 && !effects.isEmpty()) {
            // 计算效果索引：如果背包数量超过效果列表长度，则使用最后一个效果
            int effectIndex = Math.min(backpackCount, effects.size()) - 1;

            BackpackEffect effect = effects.get(effectIndex);
            Holder<MobEffect> effectType = BackpackHelper.getEffectHolder(effect.effectType);
            if (effectType == null) return;

            // 创建并应用效果
            player.addEffect(new MobEffectInstance(effectType, -1, effect.amplifier), player);

            // 记录本次添加的效果
            if (!lastAppliedEffects.contains(effectType)) {
                lastAppliedEffects.add(effectType);
            }
        }
    }

    /**
     * 饰品栏加载时将装备在背包槽的背包移回玩家主背包
     */
    public static void returnBackpackFromAccessorySlot(Player player) {
        if (player.level().isClientSide) return;

        Inventory inventory = player.getInventory();
        final int ACCESSORY_SLOT = 36 + BackpackHelper.getMaxBackpackSize(); // 背包槽位置
        ItemStack accessoryItem = inventory.getItem(ACCESSORY_SLOT);

        if (accessoryItem.getItem() instanceof BackpackItem
                && BackpackHelper.isTrinketModLoaded()
                && !Backpack.getConfig().use_dedicated_slot) {

            // 保存背包NBT数据
            BackpackStorage.saveBackpackContents(inventory, accessoryItem, true);

            // 清空背包槽
            inventory.setItem(ACCESSORY_SLOT, ItemStack.EMPTY);

            // 尝试移回主背包
            if (!inventory.add(accessoryItem)) {
                player.drop(accessoryItem, false); // 优化掉落方法
            }
        }
    }

    /**
     * 玩家死亡时保存当前装备的背包内容
     */
    public static void saveEquippedBackpackOnDeath(ServerPlayer player) {
        ItemStack equippedBackpack = BackpackPlatform.getEquipped(player);

        if (equippedBackpack.getItem() instanceof BackpackItem) {
            // 根据配置规则保存背包内容
            BackpackStorage.saveBackpackContents(
                    player.getInventory(),
                    equippedBackpack,
                    BackpackPlatform.getEmptyRule(player)
            );
        }
    }

    /**
     * 为玩家恢复非空背包的内容
     */
    public static void restoreNonEmptyBackpack(Player player) {
        ItemStack equippedBackpack = BackpackPlatform.getEquipped(player);

        // 检查背包是否包含物品组件
        if (equippedBackpack.has(BackpackPlatform.getBackpackItemsComponent())) {
            BackpackStorage.restoreBackpackContents(player.getInventory(), equippedBackpack);
        }
    }
}
