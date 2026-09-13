package com.yyz.yyzsbackpack.mixin.minecraft.container.creative;

import com.yyz.yyzsbackpack.api.IBackpackScreen;
import com.yyz.yyzsbackpack.api.helper.BackpackMenuHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import com.yyz.yyzsbackpack.inventory.BackpackSlot;
import com.yyz.yyzsbackpack.network.packets.control.QuickMoveC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;
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
    @Nullable
    private Slot destroyItemSlot;

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

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void onExtractBackgroundReturn(CallbackInfo ci) {
        BackpackScreenHelper.setupBackpackSlots((CreativeModeInventoryScreen) (Object) this);
    }

    @Inject(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onExtractBackgroundInvoke(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
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
     * 原版会把 destroyItemSlot 接在 46，而 inventoryMenu 的背包也从 46 开始。
     * 回包 containerId=0 会打到 ItemPickerMenu，必须把垃圾桶挪到背包后面，两边下标才一致。
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
     * 非 INVENTORY 页原版用 slot.index - slots.size() + 45 同步快捷栏。
     * 追加背包槽后该值变成负数，服务端当丢弃。改成 clicked + broadcastChanges。
     */
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onBackpackSlotClicked(Slot slot, int slotId, int buttonNum, ClickType input, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;

        if (input == ClickType.QUICK_CRAFT && !screen.isInventoryOpen()) {
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
            return;
        }

        if (slot != null
                && !screen.isInventoryOpen()
                && slot.container == player.getInventory()
                && slot.getContainerSlot() >= 0
                && slot.getContainerSlot() < 9) {
            screen.getMenu().clicked(slot.index, buttonNum, input, player);
            player.inventoryMenu.broadcastChanges();
            ci.cancel();
        }
    }

    /**
     * Alt+点击走 QuickMoveC2SPacket，服务端读的是 inventoryMenu 下标。
     * 创造菜单里背包槽下标与 inventoryMenu 不一致，需要转换后再发包。
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0 || !Screen.hasAltDown()) return;

        Slot slot = this.hoveredSlot;
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
            FriendlyByteBuf buf = PacketByteBufs.create();
            QuickMoveC2SPacket.write(buf, new QuickMoveC2SPacket(targetIndex));
            ClientPlayNetworking.send(QuickMoveC2SPacket.ID, buf);
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

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double x, double y, double metal, CallbackInfoReturnable<Boolean> cir) {
        if (BackpackScreenHelper.handleMouseScrolled((AbstractContainerScreen<?>) (Object) this, x, y, metal)) {
            cir.setReturnValue(true);
        }
    }
}