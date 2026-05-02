package com.yyz.yyzsbackpack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackpackEquipSlot;
import com.yyz.yyzsbackpack.base.BackpackMenuState;
import com.yyz.yyzsbackpack.base.BackpackStorageSlot;
import com.yyz.yyzsbackpack.base.BackpackMenu;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackHelper;
import com.yyz.yyzsbackpack.util.BackpackSorter;
import com.yyz.yyzsbackpack.util.BackpackStorage;
import com.yyz.yyzsbackpack.util.SlotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements BackpackMenu {

    @Shadow public abstract ItemStack getCarried();

    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract Slot getSlot(int i);

    @Unique
    private final BackpackMenuState backpackMenuState = new BackpackMenuState();

    @Override
    public boolean isBackpackVisible() {
        return backpackMenuState.isBackpackVisible();
    }

    @Override
    public void setBackpackVisible(boolean shouldRenderBackpack) {
        backpackMenuState.setBackpackVisible(shouldRenderBackpack);
    }

    @Override
    public boolean isPreviewVisible() {
        return backpackMenuState.isPreviewVisible();
    }

    @Override
    public void setPreviewVisible(boolean renderTipBackpack) {
        backpackMenuState.setPreviewVisible(renderTipBackpack);
    }

    @Override
    public int getBackpackGuiX() {
        return backpackMenuState.getBackpackGuiX();
    }

    @Override
    public int getBackpackGuiY() {
        return backpackMenuState.getBackpackGuiY();
    }

    @Override
    public void setBackpackGuiPos(int x, int y) {
        backpackMenuState.setBackpackGuiPos(x, y);
    }

    @Override
    public int getBackpackEquipSlotX() {
        return backpackMenuState.getBackpackEquipSlotX();
    }

    @Override
    public int getBackpackEquipSlotY() {
        return backpackMenuState.getBackpackEquipSlotY();
    }

    @Override
    public void setBackpackEquipSlotPos(int x, int y) {
        backpackMenuState.setBackpackEquipSlotPos(x, y);
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void handleBackpackSwap(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        BackpackSorter.handleBackpackSwap((AbstractContainerMenu) (Object) this, this.slots, this.getCarried(), slotIndex, button, actionType, player);
    }

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void handleShiftRightClick(int i, int j, ClickType clickType, Player player, CallbackInfo ci) {
        BackpackSorter.quickMoveTo((AbstractContainerMenu)(Object)this,slots,i,j,clickType,player,ci);
    }

//    @ModifyVariable(
//            method = "doClick",
//            at = @At("HEAD"),
//            index = 1,
//            argsOnly = true
//    )
//    private int modifyJCraftingParameter(int j, @Local(argsOnly = true) ClickType clickType) {
//        if (clickType == ClickType.SWAP && j == 40) {
//            return 45;
//        }
//        return j;
//    }

    @ModifyConstant(method = "doClick", constant = @Constant(intValue = 40))
    private int adjustOffhandSlotPositionHotbar(int original) {

        return original + BackpackHelper.getSlotIndexOffset();
    }

    /**
     * 在 doClick 方法开头注入，拦截 QUICK_MOVE 操作。
     */
    @Inject(
            method = "doClick",
            at = @At("HEAD"),
            cancellable = false
    )
    private void onDoClick(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {

        // 只处理快速移动
        if (clickType != ClickType.QUICK_MOVE) return;
        // 槽位索引有效性检查
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;

        Slot sourceSlot = getSlot(slotIndex);

        if(BackpackPlatform.getIndex(player) != sourceSlot.getContainerSlot()) return;



        // 获取背包物品和容器 (玩家背包)
        ItemStack backpackStack = sourceSlot.getItem();

        if (backpackStack.isEmpty()) return;

        if(!(backpackStack.getItem() instanceof BackpackItem)) return;

        if(backpackStack.has(BackpackPlatform.getBackpackItemsComponent())) return;


        // 执行保存背包内容
        BackpackStorage.saveBackpackContents(player.getInventory(), backpackStack);
    }

//    @Redirect(method = "initializeContents", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
//    private int modifyInitializeContents(List<ItemStack> list) {
//        return slots.size();
//    }

}
