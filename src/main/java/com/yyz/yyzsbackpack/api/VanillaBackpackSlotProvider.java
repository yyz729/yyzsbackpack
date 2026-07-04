package com.yyz.yyzsbackpack.api;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VanillaBackpackSlotProvider implements BackpackSlotProvider {
    @Override
    public List<BackpackSlotReference> getSlots(Player player) {
//        return List.of(new BackpackSlotReference() {
//            @Override
//            public ItemStack getStack() {
//                return player.getItemBySlot(EquipmentSlot.CHEST);
//            }
//            @Override
//            public void setStack(ItemStack stack) {
//                player.setItemSlot(EquipmentSlot.CHEST, stack);
//            }
//        });

        List<BackpackSlotReference> slots = new ArrayList<>();

        // 1. 添加所有普通物品栏（36个槽位）
        for (int i = 0; i < 36; i++) {
            final int index = i;
            slots.add(new BackpackSlotReference() {
                @Override
                public ItemStack getStack() {
                    return player.getInventory().getItem(index);
                }

                @Override
                public void setStack(ItemStack stack) {
                    player.getInventory().setItem(index, stack);
                }
            });
        }

        // 2. 添加胸甲槽位
        slots.add(new BackpackSlotReference() {
            @Override
            public ItemStack getStack() {
                return player.getItemBySlot(EquipmentSlot.CHEST);
            }

            @Override
            public void setStack(ItemStack stack) {
                player.setItemSlot(EquipmentSlot.CHEST, stack);
            }
        });

        return slots;
    }
}