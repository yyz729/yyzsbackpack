package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.api.BackpackSlotProvider;
import com.yyz.yyzsbackpack.api.BackpackSlotReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class VanillaBackpackSlotProvider implements BackpackSlotProvider {
    @Override
    public List<BackpackSlotReference> getSlots(Player player) {
        return List.of(new BackpackSlotReference() {
            @Override
            public ItemStack getStack() {
                return player.getItemBySlot(EquipmentSlot.CHEST);
            }
            @Override
            public void setStack(ItemStack stack) {
                player.setItemSlot(EquipmentSlot.CHEST, stack);
            }
        });
    }
}