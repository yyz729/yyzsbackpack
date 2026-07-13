package com.yyz.yyzsbackpack.api.helper;

import com.yyz.yyzsbackpack.api.IBackpackSlots;
import com.yyz.yyzsbackpack.api.IBackpackSlot;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.provider.VanillaBackpackSlotProvider;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BackpackSlotHelper {
    private static final List<IBackpackSlots> SLOT_PROVIDERS = new ArrayList<>();

    static {
        SLOT_PROVIDERS.add(new VanillaBackpackSlotProvider()); // 默认注册
    }

    public static void registerSlotProvider(IBackpackSlots provider) {
        SLOT_PROVIDERS.add(provider);
    }

    public static List<IBackpackSlot> getAllBackpackSlots(Player player) {
        List<IBackpackSlot> slots = new ArrayList<>();
        for (IBackpackSlots provider : SLOT_PROVIDERS) {
            slots.addAll(provider.getSlots(player));
        }

        slots.removeIf(ref -> !(ref.getStack().getItem() instanceof BackpackItem));
        return slots;
    }

    /**
     * 获取玩家当前选中的背包物品。
     */
    public static ItemStack getSelectedBackpack(Player player) {
        List<IBackpackSlot> slots = getAllBackpackSlots(player);
        if (slots.isEmpty()) return ItemStack.EMPTY;
        int idx = getSelectedIndex(player);
        if (idx < 0 || idx >= slots.size()) {
            idx = 0;
            setSelectedIndex(player, idx);
        }
        return slots.get(idx).getStack();
    }

    private static final String SELECTED_INDEX_KEY = "BackpackSelectedIndex";


//    public static int getSelectedIndex(Player player) {
//        CustomData data = player.get(DataComponents.CUSTOM_DATA);
//        if (data != null) {
//            CompoundTag tag = data.copyTag();
//            return tag.getIntOr(SELECTED_INDEX_KEY, 0);
//        }
//        return 0;
//    }
//
//    public static void setSelectedIndex(Player player, int index) {
//        CustomData old = player.get(DataComponents.CUSTOM_DATA);
//        CompoundTag tag = old != null ? old.copyTag() : new CompoundTag();
//        tag.putInt(SELECTED_INDEX_KEY, index);
//        player.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(tag));
//    }

    public static int getSelectedIndex(Player player) {
            return ((IBackpackData)player).yyzsbackpack$getSyncedBackpackIndex();
    }

    public static void setSelectedIndex(Player player, int index) {

        ((IBackpackData)player).yyzsbackpack$setSyncedBackpackIndex(index);
    }
    /**
     * 获取玩家所有背包物品列表（仅含有效背包）。
     */
    public static List<ItemStack> getAllBackpackStacks(Player player) {
        return getAllBackpackSlots(player).stream()
                .map(IBackpackSlot::getStack)
                .toList();
    }

    /**
     * 通过实体网络 ID 获取已同步的背包物品
     */
    public static ItemStack getSyncedBackpack(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return ItemStack.EMPTY;

        Entity entity = level.getEntity(entityId);
        if (entity instanceof IBackpackData backpackData) {
            return backpackData.yyzsbackpack$getSyncedBackpack();
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getSyncedBackpack(Player player) {
        if (player instanceof IBackpackData backpackData) {
            return backpackData.yyzsbackpack$getSyncedBackpack();
        }
        return ItemStack.EMPTY;
    }

    public static int getBackpackSize(Player player) {
        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        if (backpack.getItem() instanceof BackpackItem backpackItem) {
            BackpackData data = backpackItem.getData();
            if (data != null) {
                return data.size();
            }
        }
        return 0;
    }
}