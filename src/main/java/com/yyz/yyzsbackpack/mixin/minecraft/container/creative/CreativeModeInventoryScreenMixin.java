package com.yyz.yyzsbackpack.mixin.minecraft.container.creative;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import com.yyz.yyzsbackpack.network.packets.control.QuickMoveC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<ItemPickerMenu> implements IBackpackScreen {

    @Shadow
    private @Nullable Slot destroyItemSlot;

    public CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public String yyzsbackpack$getScreenType() {
        return "CreativeModeInventoryScreen";
    }

    @Override
    public boolean yyzsbackpack$allowQuickMove() {
        return false;
    }

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((CreativeModeInventoryScreen) (Object) this);
    }

    @Inject(
            method = "extractBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onExtractBackgroundInvoke(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        BackpackScreenHelper.addBackpackBackground(screen, graphics, mouseX, mouseY, partialTick);
        BackpackScreenHelper.addBackpackTabs(screen);
        BackpackScreenHelper.addBackpackScrollbar(screen);
        BackpackScreenHelper.addBackpackTitle(screen, graphics, partialTick);
        BackpackScreenHelper.addBackpackControls(screen);
    }

    /**
     * 生存物品栏页会把 inventoryMenu 的全部槽（含背包）包成 SlotWrapper，
     * 位置按原版公式会全部挤到快捷栏行，且 instanceof BackpackSlot 失败。
     * 切走该页时再从 originalSlots 恢复，其中已包含构造时加入的背包槽。
     */
    @Inject(method = "selectTab", at = @At("RETURN"))
    private void onSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
        ItemPickerMenu menu = screen.getMenu();
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (tab.getType() == CreativeModeTab.Type.INVENTORY) {
            AbstractContainerMenu invMenu = player.inventoryMenu;
            for (int i = menu.slots.size() - 1; i >= 0; i--) {
                if (i < invMenu.slots.size() && invMenu.slots.get(i) instanceof BackpackSlot) {
                    menu.slots.remove(i);
                }
            }
            if (this.destroyItemSlot != null) {
                menu.slots.remove(this.destroyItemSlot);
            }
        }

        if (BackpackMenuHelper.getBackpackSlotStart(menu) < 0) {
            BackpackMenuHelper.addBackpackSlotsIfPresent(menu, player.getInventory());
        }

        if (tab.getType() == CreativeModeTab.Type.INVENTORY && this.destroyItemSlot != null) {
            this.destroyItemSlot.index = menu.slots.size();
            menu.slots.add(this.destroyItemSlot);
        }
    }

    /**
     * ItemPickerMenu 只存在于客户端。背包点击必须转发到两端都有的 inventoryMenu。
     * 生存物品栏页原版会把槽强转 SlotWrapper，背包槽也必须在此拦截，避免 ClassCastException。
     */
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onBackpackSlotClicked(@Nullable Slot slot, int slotId, int buttonNum, ContainerInput input, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;

        if (input == ContainerInput.QUICK_CRAFT && !screen.isInventoryOpen()) {
            int targetIndex = slot == null
                    ? slotId
                    : yyzsbackpack$findInventoryMenuSlot(player.inventoryMenu, slot);
            if (slot == null || targetIndex >= 0) {
                player.inventoryMenu.clicked(targetIndex, buttonNum, input, player);
                player.inventoryMenu.broadcastChanges();
                ci.cancel();
                return;
            }
        }

        if (slot instanceof BackpackSlot) {
            int targetIndex = yyzsbackpack$findInventoryMenuBackpackIndex(player.inventoryMenu, slot);
            if (targetIndex >= 0) {
                player.inventoryMenu.clicked(targetIndex, buttonNum, input, player);
                player.inventoryMenu.broadcastChanges();
            }
            ci.cancel();
        }

    }

    /**
     * Alt+点击走 QuickMoveC2SPacket，服务端读的是 inventoryMenu 下标。
     * 创造菜单里背包槽下标与 inventoryMenu 不一致，需要转换后再发包。
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 0 || !event.hasAltDown()) return;

        Slot slot = ((ScreenAccessor<?>) this).invokeGetHoveredSlot(event.x(), event.y());
        if (slot == null) return;

        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;

        if (!screen.isInventoryOpen()) return;

        if (slot.getItem().isEmpty()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        AbstractContainerMenu invMenu = player.inventoryMenu;
        int targetIndex = -1;

        if (slot instanceof BackpackSlot) {
            targetIndex = yyzsbackpack$findInventoryMenuBackpackIndex(invMenu, slot);
        } else {
            int pos = screen.getMenu().slots.indexOf(slot);
            if (pos >= 0 && pos < invMenu.slots.size()) {
                targetIndex = pos;
            }
        }

        if (targetIndex >= 0) {
            ClientPlayNetworking.send(new QuickMoveC2SPacket(targetIndex));
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static int yyzsbackpack$findInventoryMenuBackpackIndex(AbstractContainerMenu invMenu, Slot backpackSlot) {
        int containerSlot = backpackSlot.getContainerSlot();
        for (int i = 0; i < invMenu.slots.size(); i++) {
            Slot s = invMenu.slots.get(i);
            if (s instanceof BackpackSlot && s.getContainerSlot() == containerSlot) {
                return i;
            }
        }
        return -1;
    }

    @Unique
    private static int yyzsbackpack$findInventoryMenuSlot(AbstractContainerMenu invMenu, Slot sourceSlot) {
        for (int i = 0; i < invMenu.slots.size(); i++) {
            Slot s = invMenu.slots.get(i);
            if (s.container == sourceSlot.container
                    && s.getContainerSlot() == sourceSlot.getContainerSlot()) {
                return i;
            }
        }
        return -1;
    }
}