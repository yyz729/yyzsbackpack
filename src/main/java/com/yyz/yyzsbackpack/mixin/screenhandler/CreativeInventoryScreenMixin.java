package com.yyz.yyzsbackpack.mixin.screenhandler;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    // 常量定义
    private static final int HIDDEN_SLOT_POSITION = -2000;
    private static final int OFFHAND_SLOT_X = 35;
    private static final int OFFHAND_SLOT_Y = 20;
    private static final int DELETE_SLOT_X = 173;
    private static final int DELETE_SLOT_Y = 112;
    private static final int SPECIAL_SLOT_46_X = 128; // 110 + 18
    private static final int SPECIAL_SLOT_46_Y = 20;

    @Shadow private static ItemGroup selectedTab;
    @Shadow @Nullable private List<Slot> slots;
    @Shadow @Nullable private Slot deleteItemSlot;
    @Shadow @Final private static SimpleInventory INVENTORY;
    @Shadow private TextFieldWidget searchBox;
    @Shadow private float scrollPosition;
    @Shadow protected abstract void search();

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "setSelectedTab", at = @At("HEAD"), cancellable = true)
    private void addSlots(ItemGroup group, CallbackInfo ci) {
        if (group.getType() != ItemGroup.Type.INVENTORY) {
            return;
        }

        ItemGroup previousTab = selectedTab;
        selectedTab = group;

        resetScreenState();
        rebuildSlots();
        updateSearchBox(previousTab, group);

        ci.cancel();
    }

    @Unique
    private void resetScreenState() {
        this.cursorDragSlots.clear();
        ((CreativeInventoryScreen.CreativeScreenHandler) this.handler).itemList.clear();
        this.endTouchDrag();
    }

    @Unique
    private void rebuildSlots() {
        ScreenHandler playerScreenHandler = this.client.player.playerScreenHandler;

        // 保存原始槽位
        if (this.slots == null) {
            this.slots = ImmutableList.copyOf(((CreativeInventoryScreen.CreativeScreenHandler) this.handler).slots);
        }

        // 清空当前槽位
        ((CreativeInventoryScreen.CreativeScreenHandler) this.handler).slots.clear();

        // 重建所有槽位
        for (int i = 0; i < playerScreenHandler.slots.size(); i++) {
            int x, y;

            // 特殊槽位46：放在副手槽右侧
            if (i == 46) {
                x = SPECIAL_SLOT_46_X;
                y = SPECIAL_SLOT_46_Y;
            }
            // 装备槽位（头盔/胸甲/护腿/靴子）
            else if (i >= 5 && i < 9) {
                int slotOffset = i - 5;
                int row = slotOffset / 2;
                int column = slotOffset % 2;
                x = 54 + row * 54;
                y = 6 + column * 27;
            }
            // 隐藏槽位（0-4）
            else if (i >= 0 && i < 5) {
                x = HIDDEN_SLOT_POSITION;
                y = HIDDEN_SLOT_POSITION;
            }
            // 副手槽位
            else if (i == 45) {
                x = OFFHAND_SLOT_X;
                y = OFFHAND_SLOT_Y;
            }
            // 快捷栏 (36-44)
            else if (i >= 36 && i < 45) {
                int baseIndex = i - 9;
                int gridX = baseIndex % 9;
                x = 9 + gridX * 18;
                y = 112;
            }
            // 主背包 (9-35)
            else if (i >= 9 && i < 36) {
                int baseIndex = i - 9;
                int gridX = baseIndex % 9;
                int gridY = baseIndex / 9;
                x = 9 + gridX * 18;
                y = 54 + gridY * 18;
            }
            // 其他槽位（自定义处理）
            else {
                int baseIndex = i - 9;
                int k = baseIndex - 2;
                int l = k % 9;
                int m = k / 9;
                x = (-m + 3) * 18 - 7;
                y = l * 18 - 12;
            }

            Slot originalSlot = playerScreenHandler.slots.get(i);
            Slot newSlot = new CreativeInventoryScreen.CreativeSlot(originalSlot, i, x, y);
            ((CreativeInventoryScreen.CreativeScreenHandler) this.handler).slots.add(newSlot);
        }

        // 添加删除物品槽
        this.deleteItemSlot = new Slot(INVENTORY, 0, DELETE_SLOT_X, DELETE_SLOT_Y);
        ((CreativeInventoryScreen.CreativeScreenHandler) this.handler).slots.add(this.deleteItemSlot);
    }

    @Unique
    private void updateSearchBox(ItemGroup previousTab, ItemGroup currentTab) {
        if (currentTab.getType() == ItemGroup.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setFocusUnlocked(false);
            this.searchBox.setFocused(true);
            if (previousTab != currentTab) {
                this.searchBox.setText("");
            }
            this.search();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setFocusUnlocked(true);
            this.searchBox.setFocused(false);
            this.searchBox.setText("");
        }

        // 重置滚动位置
        this.scrollPosition = 0.0F;
        ((CreativeInventoryScreen.CreativeScreenHandler) this.handler).scrollItems(0.0F);
    }
}