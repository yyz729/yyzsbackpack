package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackEffect;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    // 保存背包内容到NBT
    public static void saveBackpackContents(Container inventory, ItemStack backpackStack, boolean b) {
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int numSlots = backpackItem.getBackpackType().getSize();

        ListTag itemsTag = new ListTag();
        for (int i = 0; i < numSlots; i++) {
            int slotIndex = 36 + i;
            ItemStack stack = inventory.getItem(slotIndex);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                itemsTag.add(itemTag);
                if(b) {
                    inventory.setItem(slotIndex, ItemStack.EMPTY);
                }
            }
        }

        CompoundTag nbt = backpackStack.getOrCreateTag();
        nbt.put("BackpackItems", itemsTag);
    }

    // 从NBT恢复背包内容
    public static void restoreBackpackContents(Container inventory, ItemStack backpackStack) {
        CompoundTag nbt = backpackStack.getTag();
        if (nbt == null || !nbt.contains("BackpackItems", Tag.TAG_LIST)) {
            return;
        }

        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int numSlots = backpackItem.getBackpackType().getSize();

        ListTag itemsTag = nbt.getList("BackpackItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag itemTag = itemsTag.getCompound(i);
            int slotIndex = itemTag.getInt("Slot");
            if (slotIndex >= 0 && slotIndex < numSlots) {
                ItemStack stack = ItemStack.of(itemTag);
                inventory.setItem(36 + slotIndex, stack);
            }
        }

        nbt.remove("BackpackItems");
        if (nbt.isEmpty()) {
            backpackStack.setTag(null);
        }
    }

    public static int countNonEmptyBackpacks(Container playerInventory) {
        int nonEmptyBackpackCount = 0;

        // 遍历玩家物品栏所有槽位
        for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
            ItemStack stack = playerInventory.getItem(slot);

            // 检查是否是背包物品
            if (stack.getItem() instanceof BackpackItem) {
                CompoundTag nbt = stack.getTag();
                if (nbt != null && nbt.contains("BackpackItems", Tag.TAG_LIST)) {
                    ListTag backpackItems = nbt.getList("BackpackItems", Tag.TAG_COMPOUND);

                    // 检查背包是否有存储内容
                    for (int i = 0; i < backpackItems.size(); i++) {
                        CompoundTag itemTag = backpackItems.getCompound(i);
                        ItemStack content = ItemStack.of(itemTag);
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



    public static void updateEffectsByBackpackCount(Player player, List<MobEffect> lastAppliedEffects) {
        int backpackCount = BackpackStorage.countNonEmptyBackpacks(player.getInventory());

        // 移除不再需要的老效果
        for (MobEffect effect : new ArrayList<>(lastAppliedEffects)) {
            player.removeEffect(effect);
            lastAppliedEffects.remove(effect);
        }

        // 检查条件并应用新效果
        List<BackpackEffect> effects = Backpack.getConfig().backpack_multi_effects;
        if (backpackCount > 0 && !effects.isEmpty()) {
            // 计算效果索引：如果背包数量超过效果列表长度，则使用最后一个效果
            int effectIndex = Math.min(backpackCount, effects.size()) - 1;

            BackpackEffect effect = effects.get(effectIndex);
            MobEffect effectType = BackpackHelper.getEffectHolder(effect.effectType);
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

        // 检查背包NBT是否包含背包物品
        CompoundTag nbt = equippedBackpack.getTag();
        if (nbt != null && nbt.contains("BackpackItems", Tag.TAG_LIST)) {
            BackpackStorage.restoreBackpackContents(player.getInventory(), equippedBackpack);
        }
    }
}
