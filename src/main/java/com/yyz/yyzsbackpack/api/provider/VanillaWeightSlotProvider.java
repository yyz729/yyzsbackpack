package com.yyz.yyzsbackpack.api.provider;

import com.yyz.yyzsbackpack.api.IBackpackSlot;
import com.yyz.yyzsbackpack.api.IWeightSlots;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VanillaWeightSlotProvider implements IWeightSlots {

    @Override
    public List<IBackpackSlot> getWeightSlots(Player player) {
        List<IBackpackSlot> slots = new ArrayList<>();

        // 主物品栏 36 格（0~35）
        for (int i = 0; i < 36; i++) {
            final int index = i;
            slots.add(new IBackpackSlot() {
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

        slots.add(new IBackpackSlot() {
            @Override
            public ItemStack getStack() {
                return player.getItemBySlot(EquipmentSlot.CHEST);
            }

            @Override
            public void setStack(ItemStack stack) {
                player.setItemSlot(EquipmentSlot.CHEST, stack);
            }
        });

        slots.add(new IBackpackSlot() {
            @Override
            public ItemStack getStack() {
                return player.getItemBySlot(EquipmentSlot.OFFHAND);
            }

            @Override
            public void setStack(ItemStack stack) {
                player.setItemSlot(EquipmentSlot.OFFHAND, stack);
            }
        });

        slots.add(new IBackpackSlot() {
            @Override
            public ItemStack getStack() {
                return player.inventoryMenu.getCarried();
            }

            @Override
            public void setStack(ItemStack stack) {
                player.inventoryMenu.setCarried(stack);
            }
        });

        return slots;
    }
}