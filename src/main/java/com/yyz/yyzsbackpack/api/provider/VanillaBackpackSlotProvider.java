package com.yyz.yyzsbackpack.api.provider;

import com.yyz.yyzsbackpack.api.IBackpackSlots;
import com.yyz.yyzsbackpack.api.IBackpackSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VanillaBackpackSlotProvider implements IBackpackSlots {
    @Override
    public List<IBackpackSlot> getSlots(Player player) {
        List<IBackpackSlot> slots = new ArrayList<>();
        //所有物品栏
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

        //胸甲
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

        return slots;
    }
}