package com.yyz.yyzsbackpack.item;


import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.data.BackpackDataLoader;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BackpackItem extends Item implements EquipmentSlotProvider, DyeableLeatherItem {

    private final String type;

    public BackpackItem(String type, Properties settings) {
        super(settings);
        this.type = type;
    }

    public BackpackData getData() {
        return BackpackDataLoader.getData(type);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("Items", new ListTag());
        return stack;
    }
}