package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.config.BackpackEffect;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.BackpackStorage;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Unique
    ServerPlayer player = (ServerPlayer) (Object) this;

    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        Inventory inventory = player.getInventory();
        ItemStack stack = BackpackPlatform.getEquipped(player);

        if (stack.getItem() instanceof BackpackItem) {
            // 保存背包内容并清空槽位
            BackpackStorage.saveBackpackContents(inventory, stack, BackpackPlatform.getEmptyRule(player));
        }
    }
    @Inject(method = "initMenu", at = @At("HEAD"))
    private void onOpenMenu(CallbackInfo ci) {


        if (player.level().isClientSide) return;

        Inventory inventory = player.getInventory();
        ItemStack stackInSlot36 = inventory.getItem(36+54);

        // 检查36号槽位是否有背包
        if (stackInSlot36.getItem() instanceof BackpackItem && BackpackHelper.isTrinketModLoaded() && !Backpack.getConfig().use_dedicated_slot) {
            // 保存背包内容到NBT
            BackpackStorage.saveBackpackContents(inventory, stackInSlot36, true);

            // 从槽位移除背包
            inventory.setItem(36+54, ItemStack.EMPTY);

            // 将背包放回玩家背包
            if (!inventory.add(stackInSlot36)) {
                // 如果背包满了，则掉落物品
                player.spawnAtLocation(player.level(),stackInSlot36, 1.0F);
            }
        }
    }

    private List<Holder<MobEffect>> lastAppliedEffects = new ArrayList<>();

    @Inject(method = "tick", at = @At("RETURN"))
    private void addSlot(CallbackInfo ci) {
        if (BackpackPlatform.getEquipped(player).has(BackpackPlatform.getBackpackItemsComponent())) {
            BackpackStorage.restoreBackpackContents(player.getInventory(), BackpackPlatform.getEquipped(player));
        }

        int backpackCount = BackpackStorage.countNonEmptyBackpacks(player.getInventory());

        // 1. 移除不再需要的老效果
        for (Holder<MobEffect> effect : new ArrayList<>(lastAppliedEffects)) {
            player.removeEffect(effect);
            lastAppliedEffects.remove(effect);
        }

        // 2. 检查条件并应用新效果
        if (backpackCount > 0 && Backpack.getConfig().backpack_multi_effects.size() >= backpackCount) {
            BackpackEffect effect = Backpack.getConfig().backpack_multi_effects.get(backpackCount - 1);
            Holder<MobEffect> effectType = BackpackHelper.getEffectHolder(effect.effectType);
            if(effectType == null) return;
            // 创建并应用效果
            player.addEffect(new MobEffectInstance(effectType, -1, effect.amplifier), player);

            // 记录本次添加的效果
            if (!lastAppliedEffects.contains(effectType)) {
                lastAppliedEffects.add(effectType);
            }
        }
    }

}