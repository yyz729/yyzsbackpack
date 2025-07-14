package com.yyz.yyzsbackpack.fabric.mixin.trinkets;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackManager;
import com.yyz.yyzsbackpack.item.BackpackItem;
import dev.emi.trinkets.SurvivalTrinketSlot;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SurvivalTrinketSlot.class)
public abstract class SurvivalTrinketSlotMixin extends Slot {


    public SurvivalTrinketSlotMixin(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }

    @Override
    public void onTake(@NotNull Player player, ItemStack backpackStack) {
        if (backpackStack.getItem() instanceof BackpackItem && !Backpack.getConfig().force_slot) {
            BackpackManager.saveBackpackContents(player.getInventory(), backpackStack, true);
        }
        super.onTake(player, backpackStack);
    }

    @Override
    public void setByPlayer(@NotNull ItemStack newBackpackStack) {
        // 获取 TrinketInventory 实例
        if (this.container instanceof TrinketInventory trinketInv && !Backpack.getConfig().force_slot) {
            // 通过 TrinketComponent 获取实体
            TrinketComponent comp = trinketInv.getComponent();
            LivingEntity entity = comp.getEntity();

            // 确保实体是玩家
            if (entity instanceof Player player) {
                Inventory inventory = player.getInventory();

                ItemStack oldBackpackStack = this.getItem();
                if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.saveBackpackContents(inventory, oldBackpackStack, true);
                }

                super.setByPlayer(newBackpackStack);

                if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.restoreBackpackContents(inventory, newBackpackStack);
                }
                return; // 提前返回
            }
        }

        // 如果无法获取玩家，执行默认行为
        super.setByPlayer(newBackpackStack);
    }
}
