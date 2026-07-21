package com.yyz.yyzsbackpack.api.helper;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.IBackpackSlot;
import com.yyz.yyzsbackpack.api.IBackpackSlots;
import com.yyz.yyzsbackpack.api.IWeightSlots;
import com.yyz.yyzsbackpack.api.provider.VanillaBackpackSlotProvider;
import com.yyz.yyzsbackpack.api.provider.VanillaWeightSlotProvider;
import com.yyz.yyzsbackpack.component.BackpackIdComponent;
import com.yyz.yyzsbackpack.component.ModComponents;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.effect.ModEffects;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BackpackSlotHelper {
    private static final List<IBackpackSlots> SLOT_PROVIDERS = new ArrayList<>();
    private static final List<IWeightSlots> WEIGHT_PROVIDERS = new ArrayList<>();

    static {
        SLOT_PROVIDERS.add(new VanillaBackpackSlotProvider()); // 默认注册
        WEIGHT_PROVIDERS.add(new VanillaWeightSlotProvider());
    }

    /**
     * 注册新的背包识别槽位提供者
     */
    public static void registerSlotProvider(IBackpackSlots provider) {
        SLOT_PROVIDERS.add(provider);
    }

    /**
     * 注册新的负重槽位提供者
     */
    public static void registerWeightProvider(IWeightSlots provider) {
        WEIGHT_PROVIDERS.add(provider);
    }

    /**
     * 获取所有背包识别槽位
     */
    public static List<IBackpackSlot> getAllBackpackSlots(Player player) {
        List<IBackpackSlot> slots = new ArrayList<>();
        for (IBackpackSlots provider : SLOT_PROVIDERS) {
            slots.addAll(provider.getSlots(player));
        }

        slots.removeIf(ref -> !(ref.getStack().getItem() instanceof BackpackItem));

        for (IBackpackSlot ref : slots) {
            ensureBackpackId(ref.getStack());
        }
        return slots;
    }

    private static void ensureBackpackId(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BackpackItem)) return;
        if (!stack.has(ModComponents.BACKPACK_ID)) {
            stack.set(ModComponents.BACKPACK_ID, new BackpackIdComponent(UUID.randomUUID()));
        }
    }

    /**
     * 获取玩家当前选中的背包物品
     */
    public static ItemStack getSelectedBackpack(Player player) {
        List<IBackpackSlot> slots = getAllBackpackSlots(player);
        if (slots.isEmpty()) return ItemStack.EMPTY;

        int idx = getSelectedIndex(player);
        if (idx < 0 || idx >= slots.size()) {
            idx = 0;
            setSelectedIndex(player, idx);
        }

        ItemStack currentStack = slots.get(idx).getStack();
        String currentUUID = null;
        if (!currentStack.isEmpty() && currentStack.getItem() instanceof BackpackItem) {
            BackpackIdComponent idComp = currentStack.get(ModComponents.BACKPACK_ID);
            if (idComp != null) {
                currentUUID = idComp.id().toString(); // 转为字符串
            }
        }

        String expectedUUID = ((IBackpackData) player).yyzsbackpack$getSelectedBackpackUuid();

        if (expectedUUID != null && !expectedUUID.isEmpty() && currentUUID != null && !expectedUUID.equals(currentUUID)) {
            for (int i = 0; i < slots.size(); i++) {
                ItemStack stack = slots.get(i).getStack();
                if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                    BackpackIdComponent idComp = stack.get(ModComponents.BACKPACK_ID);
                    if (idComp != null && expectedUUID.equals(idComp.id().toString())) {
                        setSelectedIndex(player, i);
                        return stack;
                    }
                }
            }
            ((IBackpackData) player).yyzsbackpack$setSelectedBackpackUuid(null);
        } else if ((expectedUUID == null || expectedUUID.isEmpty()) && currentUUID != null) {
            ((IBackpackData) player).yyzsbackpack$setSelectedBackpackUuid(currentUUID);
        }

        return slots.get(idx).getStack();
    }

    /**
     * 获取玩家当前选中的背包物品序号
     */
    public static int getSelectedIndex(Player player) {
        return ((IBackpackData)player).yyzsbackpack$getSyncedBackpackIndex();
    }

    /**
     * 获设置家当前选中的背包物品序号
     */
    public static void setSelectedIndex(Player player, int index) {
        ((IBackpackData) player).yyzsbackpack$setSyncedBackpackIndex(index);

        List<IBackpackSlot> slots = getAllBackpackSlots(player);
        if (index >= 0 && index < slots.size()) {
            ItemStack stack = slots.get(index).getStack();
            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                BackpackIdComponent idComp = stack.get(ModComponents.BACKPACK_ID);
                if (idComp != null) {
                    ((IBackpackData) player).yyzsbackpack$setSelectedBackpackUuid(idComp.id().toString());
                    return;
                }
            }
        }
        ((IBackpackData) player).yyzsbackpack$setSelectedBackpackUuid(null);
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

    /**
     * 获取玩家已同步的背包物品
     */
    public static ItemStack getSyncedBackpack(Player player) {
        if (player instanceof IBackpackData backpackData) {
            return backpackData.yyzsbackpack$getSyncedBackpack();
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取玩家当前背包大小
     */
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

    /**
     * 获取玩家身上所有参与负重计算的槽位引用
     */
    public static List<IBackpackSlot> getAllWeightSlots(Player player) {
        List<IBackpackSlot> all = new ArrayList<>();
        for (IWeightSlots provider : WEIGHT_PROVIDERS) {
            all.addAll(provider.getWeightSlots(player));
        }
        return all;
    }

    /**
     * 统计玩家身上背包（BackpackItem）的数量
     */
    public static int countBackpacks(Player player) {
        return (int) getAllWeightSlots(player).stream()
                .map(IBackpackSlot::getStack)
                .filter(stack -> !stack.isEmpty() && stack.getItem() instanceof BackpackItem)
                .count();
    }

    /**
     * 更新玩家负重效果
     */
    public static void updateWeightEffect(Player player) {
        if (player.level().isClientSide()) return;

        int backpackCount = BackpackSlotHelper.countBackpacks(player);
        boolean hasEffect = player.hasEffect(ModEffects.HEAVY);

        if (backpackCount > Backpack.getConfig().heavy) {
            if (!hasEffect) {
                player.addEffect(new MobEffectInstance(
                        ModEffects.HEAVY,
                        MobEffectInstance.INFINITE_DURATION,
                        0,
                        false,
                        false,
                        true
                ));
            }
        } else {
            if (hasEffect) {
                player.removeEffect(ModEffects.HEAVY);
            }
        }
    }
}