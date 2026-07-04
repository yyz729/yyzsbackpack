package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.IBackpackMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin{

    @Inject(method = "clicked", at = @At("HEAD"))
    private void logClick(int slotIndex, int buttonNum, ContainerInput containerInput,
                          Player player, CallbackInfo ci) {
        // 获取当前菜单实例（混入对象）
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        // 构造槽位信息
        String slotInfo;
        if (slotIndex >= 0 && slotIndex < menu.slots.size()) {
            Slot slot = menu.slots.get(slotIndex);
            ItemStack stack = slot.getItem();
            slotInfo = String.format("Slot[%d] (%s) = %s",
                    slotIndex,
                    slot.getClass().getSimpleName(),
                    stack.isEmpty() ? "empty" : stack.getDisplayName().getString() + " x" + stack.getCount());
        } else {
            slotInfo = "Slot index " + slotIndex + " (outside)";
        }

        // 打印日志
        Backpack.LOGGER.info("Container Click - Player: {}, Slot: {}, Button: {}, Input: {}, Carried: {}",
                player.getName().getString(),
                slotInfo,
                buttonNum,
                containerInput,
                menu.getCarried().isEmpty() ? "empty" :
                        menu.getCarried().getDisplayName().getString() + " x" + menu.getCarried().getCount()
        );
    }
}
