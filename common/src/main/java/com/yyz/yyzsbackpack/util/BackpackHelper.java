package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.data.BackpackMaterialManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BackpackHelper {

    public static boolean isTrinketModLoaded() {
        return BackpackPlatform.isModLoaded("trinkets") ||
                BackpackPlatform.isModLoaded("curios") ||
                BackpackPlatform.isModLoaded("accessories");
    }

    public static int getMaxBackpackSize(){
        return BackpackMaterialManager.getMaxSize();
    }

    public static int getSlotIndexOffset(){
        return getMaxBackpackSize() + 1;
    }


    public static boolean shouldRenderBackpack(AbstractContainerMenu menu, Inventory inventory) {

        // 检查玩家是否有背包
        if (inventory != null && ((BackpackMenu)menu).isBackpackVisible()) {

            ItemStack backpackStack = BackpackPlatform.getEquipped(inventory.player);

            return backpackStack.getItem() instanceof BackpackItem;
        }

        return false;
    }

    public static int getBackpackSize(Player player){
        // 检查是否有背包物品
        ItemStack backpackStack = BackpackPlatform.getEquipped(player);
        if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
            // 基础槽位数 + 背包列数 * 9
            return 36 + backpackItem.getBackpackType().getSize();
        }
        return 36; // 没有背包时返回基础槽位数
    }

    private static Set<ResourceLocation> convertStringSetToIdentifierSet(Set<String> stringSet) {
        return stringSet.stream()
                .map(s -> {
                    try {
                        return ResourceLocation.tryParse(s);
                    } catch (ResourceLocationException e) {
                        System.err.println("Invalid Identifier: " + s);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static boolean isItemBlacklisted(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return convertStringSetToIdentifierSet(Backpack.getConfig().restricted_items).contains(id);
    }


    public static Holder<MobEffect> getEffectHolder(String effectId) {
        ResourceLocation location = ResourceLocation.tryParse(effectId);
        if (location == null) {
            return null;
        }
        Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.getHolder(
                ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), location)
        );
        return holder.orElse(null);

    }

    // 点击范围判断 - 添加偏移值支持
    public static boolean isClickOutsideExtendedBounds(Inventory inventory,
                                                       boolean outsideOriginalBounds,
                                                       double mouseX, double mouseY,
                                                       int left, int top,
                                                       int backgroundWidth, int backgroundHeight,
                                                       boolean shouldRenderBackpackExtension,
                                                       BackpackMenu renderCondition) {

        boolean inBackpackArea = false;

        if (shouldRenderBackpackExtension) {
            int width = 256;
            int height = 256;
            ItemStack backpackStack = BackpackPlatform.getEquipped(inventory.player);
            if (backpackStack.getItem() instanceof BackpackItem backpack) {
                width = backpack.getBackpackType().guiWidth();
                height = backpack.getBackpackType().guiHeight();
            }

            // 应用偏移值
            int backpackX = left - width - 1 + renderCondition.getBackpackGuiX();
            int backpackY = top + (backgroundHeight - height) / 2 + renderCondition.getBackpackGuiY();
            int backpackHeight = height;

            inBackpackArea = mouseX >= backpackX &&
                    mouseX < backpackX + width &&
                    mouseY >= backpackY &&
                    mouseY < backpackY + backpackHeight;
        }

        return outsideOriginalBounds && !inBackpackArea;
    }
}
