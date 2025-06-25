package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.api.BackPackSlot;
import com.yyz.yyzsbackpack.api.BackpackRenderCondition;
import com.yyz.yyzsbackpack.api.EquipPackSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class BackpackManager {
    public static final ResourceLocation BACKPACK_TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/backpack.png");
    public static final ResourceLocation BACKSLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "container/slot/backslot");
    public static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "textures/gui/slot.png");


    public static void updateBackpackSlotsPosition(
            AbstractContainerMenu menu,
            int backpackSlotStartIndex,
            int baseHeight,
            int xOffset,
            int yOffset
    ) {
        final int rows = 9; // 固定9行
        for (int column = 0; column < 6; column++) { // 最大6列
            for (int row = 0; row < rows; row++) {
                int slotIndex = backpackSlotStartIndex + column * rows + row;
                if (slotIndex < menu.slots.size()) {
                    Slot slot = menu.slots.get(slotIndex);
                    if(slot instanceof BackPackSlot) {
                        slot.x = - 25 - column * 18 + xOffset;
                        slot.y = (baseHeight - 166) / 2 + 3 + row * 18 + yOffset;
                    }
                }
            }
        }
    }
    public static void updateEquipmentSlotPosition(
            AbstractContainerMenu menu,
            int baseHeight,
            int xOffset,
            int yOffset
    ) {
        for (Slot slot : menu.slots) {
            if (slot instanceof EquipPackSlot) {
                slot.x = 8 + 69 + xOffset; // 水平位置
                slot.y = (baseHeight - 166) / 2 + 8 + 18 * 2 + yOffset;
                break; // 只有一个装备槽，找到后退出
            }
        }
    }

    // 背包槽位管理
    public static void addBackpackSlots(AbstractContainerMenu screenHandler, Container inventory) {

        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 9; row++) {
                final int columnIndex = column;

                screenHandler.addSlot(new BackPackSlot(inventory, row + (column + 1) * 9 + 27 + 1 ,  0 , 0) {
                    @Override
                    public boolean isActive() {
                        ItemStack backpackStack = BackpackHelper.getEquipped(Minecraft.getInstance().player);
                        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
                            return false;
                        }

                        int columns = backpackItem.getBackpackType().getColumns();
                        if (columnIndex >= columns) {
                            return false;
                        }

                        if(!(((BackpackRenderCondition)screenHandler).shouldRenderBackpack())){
                            return false;
                        }

                        return true;
                    }
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        ItemStack backpackStack = BackpackHelper.getEquipped(Minecraft.getInstance().player);
                        boolean canPlace = !(stack.getItem() instanceof BackpackItem) &&
                                backpackStack.getItem() instanceof BackpackItem &&
                                super.mayPlace(stack);

                        // 添加列数检查（仅添加这一部分）
                        if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
                            int columns = backpackItem.getBackpackType().getColumns();
                            if (columnIndex >= columns) {
                                return false;
                            }
                        }

                        return canPlace;
                    }
                });

            }
        }
    }
    public static void renderEquippackSlot(GuiGraphics guiGraphics, int x, int y){
        if(BackpackHelper.isModLoaded("trinkets") || BackpackHelper.isModLoaded("curios")) return;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,SLOT_TEXTURE,  x,  y, 0, 0, 18, 18, 18, 18);

    }
    public static void addEquipmentSlot(AbstractContainerMenu screenHandler, Container inventory) {
        if(BackpackHelper.isModLoaded("trinkets") || BackpackHelper.isModLoaded("curios")) return;
        screenHandler.addSlot(new EquipPackSlot(inventory, 36, 8 + 69 ,  8 + 18 * 2) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BackpackItem;
            }

            @Override
            public void onTake(Player player, ItemStack backpackStack) {
                if (backpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.saveBackpackContents(inventory, backpackStack);
                }
                super.onTake(player, backpackStack);
            }

            @Override
            public void setByPlayer(ItemStack newBackpackStack) {
                ItemStack oldBackpackStack = this.getItem();
                if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.saveBackpackContents(inventory, oldBackpackStack);
                }

                super.setByPlayer(newBackpackStack);

                if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
                    BackpackManager.restoreBackpackContents(inventory, newBackpackStack);
                }
            }

            @Override
            public ResourceLocation getNoItemIcon() {
                return BackpackManager.BACKSLOT_TEXTURE;
            }

        });
    }

    // 保存背包内容到数据组件
    public static void saveBackpackContents(Container inventory, ItemStack backpackStack) {
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        // 创建固定大小的列表（所有槽位，包括空）
        List<ItemStack> items = new ArrayList<>(numSlots);
        for (int i = 0; i < numSlots; i++) {
            int slotIndex = 37 + i;
            ItemStack stack = inventory.getItem(slotIndex);
            // 复制堆栈防止引用问题
            items.add(stack.copy());
            // 清空原库存槽位
            inventory.setItem(slotIndex, ItemStack.EMPTY);
        }

        // 设置数据组件
        backpackStack.set(BackpackPlatform.getBackpackItemsComponent(), items);
    }

    // 从数据组件恢复背包内容
    public static void restoreBackpackContents(Container inventory, ItemStack backpackStack) {
        // 获取数据组件
        List<ItemStack> items = backpackStack.get(BackpackPlatform.getBackpackItemsComponent());
        if (items == null) return;

        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        // 恢复物品到对应槽位
        for (int i = 0; i < Math.min(items.size(), numSlots); i++) {
            ItemStack stack = items.get(i);
            // 只恢复非空堆栈
            if (!stack.isEmpty()) {
                inventory.setItem(37 + i, stack.copy());
            }
        }

        // 移除数据组件
        backpackStack.remove(BackpackPlatform.getBackpackItemsComponent());
    }

    // 背景渲染方法 - 添加偏移值支持
    public static void renderBackpackBackground(GuiGraphics context, int x, int y,
                                                int backgroundWidth, int backgroundHeight,
                                                Inventory inventory, boolean shouldRenderBackpack,
                                                BackpackRenderCondition renderCondition) {

        if (!shouldRenderBackpack) return;

        int columns = 0;
        ItemStack stack = BackpackHelper.getEquipped(Minecraft.getInstance().player);
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            columns = backpackItem.getBackpackType().getColumns();
        }

        int width = 14 + columns * 18;
        // 应用偏移值
        int left = x - 14 - columns * 18 - 1 + renderCondition.getBackpackXOffset();
        int top = y + (backgroundHeight - 174) / 2 + renderCondition.getBackpackYOffset();
        int u = 14 * (columns - 1) + 18 * (columns - 1) * columns / 2;
        context.blit(RenderPipelines.GUI_TEXTURED,BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);
    }



    public static boolean shouldRenderBackpackExtension(AbstractContainerMenu handler, Inventory inventory) {


        // 检查玩家是否有背包
        if (inventory != null && ((BackpackRenderCondition)handler).shouldRenderBackpack()) {

            ItemStack backpackStack = BackpackHelper.getEquipped(Minecraft.getInstance().player);

            return backpackStack.getItem() instanceof BackpackItem;
        }

        return false;
    }


    // 点击范围判断 - 添加偏移值支持
    public static boolean isClickOutsideExtendedBounds(Inventory playerInventory,
                                                       boolean outsideOriginalBounds,
                                                       double mouseX, double mouseY,
                                                       int left, int top,
                                                       int backgroundWidth, int backgroundHeight,
                                                       boolean shouldRenderBackpackExtension,
                                                       BackpackRenderCondition renderCondition) {

        boolean inBackpackArea = false;

        if (shouldRenderBackpackExtension) {
            int columns = 0;
            ItemStack backpackStack = BackpackHelper.getEquipped(Minecraft.getInstance().player);
            if (backpackStack.getItem() instanceof BackpackItem backpack) {
                columns = backpack.getBackpackType().getColumns();
            }

            int backpackWidth = 14 + columns * 18;
            // 应用偏移值
            int backpackX = left - backpackWidth - 1 + renderCondition.getBackpackXOffset();
            int backpackY = top + (backgroundHeight - 174) / 2 + renderCondition.getBackpackYOffset();
            int backpackHeight = 174;

            inBackpackArea = mouseX >= backpackX &&
                    mouseX < backpackX + backpackWidth &&
                    mouseY >= backpackY &&
                    mouseY < backpackY + backpackHeight;
        }

        return outsideOriginalBounds && !inBackpackArea;
    }

    public static class Ref<T> {
        public T value;
        public Ref(T value) {
            this.value = value;
        }
    }
}