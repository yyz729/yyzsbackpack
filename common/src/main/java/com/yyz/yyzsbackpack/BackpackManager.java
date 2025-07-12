package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.base.BackPackSlot;
import com.yyz.yyzsbackpack.base.BackpackCondition;
import com.yyz.yyzsbackpack.base.EquipPackSlot;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackManager {
    public static final ResourceLocation BACKPACK_TEXTURE = new ResourceLocation(Backpack.MOD_ID, "textures/gui/backpack.png");
    public static final ResourceLocation BACKSLOT_TEXTURE = new ResourceLocation(Backpack.MOD_ID, "item/backslot");
    public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(Backpack.MOD_ID, "textures/gui//slot.png");


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
    public static void addBackpackSlots(AbstractContainerMenu screenHandler, Inventory container) {

        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 9; row++) {
                final int columnIndex = column;
                screenHandler.addSlot(new BackPackSlot(screenHandler,container, row + (column + 1) * 9 + 27  ,  columnIndex, - 25 - column * 18 , 3 + row * 18));

            }
        }
    }
    public static boolean isTrinketModLoaded() {
        return BackpackHelper.isModLoaded("trinkets") ||
                BackpackHelper.isModLoaded("curios") ||
                BackpackHelper.isModLoaded("accessories");
    }
    public static void renderEquippackSlot(InventoryMenu menu, GuiGraphics guiGraphics, int x, int y){
        if(isTrinketModLoaded() && !Backpack.getConfig().force_slot) return;
        guiGraphics.blit(SLOT_TEXTURE,  x + ((BackpackCondition)menu).getEquippackXOffset(),  y +((BackpackCondition)menu).getEquippackYOffset() , 0, 0, 18, 18, 18, 18);
    }
    public static void addEquippackSlot(AbstractContainerMenu screenHandler, Inventory inventory) {
        if(isTrinketModLoaded() && !Backpack.getConfig().force_slot) return;
        screenHandler.addSlot(new EquipPackSlot(inventory, 36+54, 8 + 69 ,  8 + 18 * 2));
    }
    // 保存背包内容到NBT
    public static void saveBackpackContents(Container inventory, ItemStack backpackStack, boolean b) {
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        ListTag itemsTag = new ListTag();
        for (int i = 0; i < numSlots; i++) {
            int slotIndex = 36 + i;
            ItemStack stack = inventory.getItem(slotIndex);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                itemsTag.add(itemTag);
                if(b) {
                    inventory.setItem(slotIndex, ItemStack.EMPTY);
                }
            }
        }

        CompoundTag nbt = backpackStack.getOrCreateTag();
        nbt.put("BackpackItems", itemsTag);
    }

    // 从NBT恢复背包内容
    public static void restoreBackpackContents(Container inventory, ItemStack backpackStack) {
        CompoundTag nbt = backpackStack.getTag();
        if (nbt == null || !nbt.contains("BackpackItems", Tag.TAG_LIST)) {
            return;
        }

        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        ListTag itemsTag = nbt.getList("BackpackItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag itemTag = itemsTag.getCompound(i);
            int slotIndex = itemTag.getInt("Slot");
            if (slotIndex >= 0 && slotIndex < numSlots) {
                ItemStack stack = ItemStack.of(itemTag);
                inventory.setItem(36 + slotIndex, stack);
            }
        }

        nbt.remove("BackpackItems");
        if (nbt.isEmpty()) {
            backpackStack.setTag(null);
        }
    }

    // 背景渲染方法 - 添加偏移值支持
    public static void renderBackpackBackground(GuiGraphics context, int x, int y,
                                                int backgroundWidth, int backgroundHeight,
                                                Inventory inventory, boolean shouldRenderBackpack,
                                                BackpackCondition renderCondition) {

        if (!shouldRenderBackpack) return;

        int columns = 0;
        ItemStack stack = BackpackHelper.getEquipped(inventory.player);
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            columns = backpackItem.getBackpackType().getColumns();
        }

        int width = 14 + columns * 18;
        // 应用偏移值
        int left = x - 14 - columns * 18 - 1 + renderCondition.getBackpackXOffset();
        int top = y + (backgroundHeight - 174) / 2 + renderCondition.getBackpackYOffset();
        int u = 14 * (columns - 1) + 18 * (columns - 1) * columns / 2;

        context.blit(BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);
    }


    public static boolean shouldRenderBackpackExtension(AbstractContainerMenu handler, Inventory inventory) {


        // 检查玩家是否有背包
        if (inventory != null && ((BackpackCondition)handler).shouldRenderBackpack()) {

            ItemStack backpackStack = BackpackHelper.getEquipped(inventory.player);

            return backpackStack.getItem() instanceof BackpackItem;
        }

        return false;
    }


    // 点击范围判断 - 添加偏移值支持
    public static boolean isClickOutsideExtendedBounds(Inventory inventory,
                                                       boolean outsideOriginalBounds,
                                                       double mouseX, double mouseY,
                                                       int left, int top,
                                                       int backgroundWidth, int backgroundHeight,
                                                       boolean shouldRenderBackpackExtension,
                                                       BackpackCondition renderCondition) {

        boolean inBackpackArea = false;

        if (shouldRenderBackpackExtension) {
            int columns = 0;
            ItemStack backpackStack = BackpackHelper.getEquipped(inventory.player);
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


    public static int getBackpackSize(Player player){
        // 检查是否有背包物品
        ItemStack backpackStack = BackpackHelper.getEquipped(player);
        if (backpackStack.getItem() instanceof BackpackItem backpackItem) {
            // 基础槽位数 + 背包列数 * 9
            return 36 + backpackItem.getBackpackType().getColumns() * 9;
        }
        return 36; // 没有背包时返回基础槽位数
    }
}