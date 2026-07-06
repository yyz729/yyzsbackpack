package com.yyz.yyzsbackpack.api.provider;

import com.yyz.yyzsbackpack.api.IBackpackSlotProvider;
import com.yyz.yyzsbackpack.api.IBackpackSlotReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VanillaBackpackSlotProvider implements IBackpackSlotProvider {
    @Override
    public List<IBackpackSlotReference> getSlots(Player player) {
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

        List<IBackpackSlotReference> slots = new ArrayList<>();

        //所有物品栏
        for (int i = 0; i < 36; i++) {
            final int index = i;
            slots.add(new IBackpackSlotReference() {
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
        slots.add(new IBackpackSlotReference() {
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