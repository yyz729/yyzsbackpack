package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class BackpackManager {
    public static final Identifier BACKPACK_TEXTURE = new Identifier(Backpack.MOD_ID, "textures/gui/backpack.png");
    public static final Identifier BACKSLOT_TEXTURE = new Identifier(Backpack.MOD_ID, "item/backslot");
    public static final Identifier SLOT_TEXTURE = new Identifier(Backpack.MOD_ID, "textures/gui//slot.png");


    // 背包槽位管理
    public static void addBackpackSlots(ScreenHandler screenHandler, Inventory inventory, int width, int height) {

        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 9; row++) {
                final int columnIndex = column;
                screenHandler.addSlot(new Slot(inventory, row + (column + 1) * 9 + 27 + 1, -25 - column * 18, (height - 166) / 2 + 3 + row * 18) {
                    @Override
                    public boolean isEnabled() {
                        ItemStack backpackStack = inventory.getStack(36);
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
                    public boolean canInsert(ItemStack stack) {
                        return !(stack.getItem() instanceof BackpackItem) && super.canInsert(stack);
                    }
                });

            }
        }
    }

    // 保存背包内容到NBT
    public static void saveBackpackContents(Inventory inventory, ItemStack backpackStack) {
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        NbtList itemsTag = new NbtList();
        for (int i = 0; i < numSlots; i++) {
            int slotIndex = 37 + i;
            ItemStack stack = inventory.getStack(slotIndex);
            if (!stack.isEmpty()) {
                NbtCompound itemTag = new NbtCompound();
                itemTag.putInt("Slot", i);
                stack.writeNbt(itemTag);
                itemsTag.add(itemTag);
                inventory.setStack(slotIndex, ItemStack.EMPTY);
            }
        }

        NbtCompound nbt = backpackStack.getOrCreateNbt();
        nbt.put("BackpackItems", itemsTag);
    }

    // 从NBT恢复背包内容
    public static void restoreBackpackContents(Inventory inventory, ItemStack backpackStack) {
        NbtCompound nbt = backpackStack.getNbt();
        if (nbt == null || !nbt.contains("BackpackItems", NbtElement.LIST_TYPE)) {
            return;
        }

        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        NbtList itemsTag = nbt.getList("BackpackItems", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < itemsTag.size(); i++) {
            NbtCompound itemTag = itemsTag.getCompound(i);
            int slotIndex = itemTag.getInt("Slot");
            if (slotIndex >= 0 && slotIndex < numSlots) {
                ItemStack stack = ItemStack.fromNbt(itemTag);
                inventory.setStack(37 + slotIndex, stack);
            }
        }

        nbt.remove("BackpackItems");
        if (nbt.isEmpty()) {
            backpackStack.setNbt(null);
        }
    }

    // GUI 渲染辅助方法
    public static void renderBackpackBackground(DrawContext context, int x, int y, int backgroundWidth, int backgroundHeight,
                                                PlayerInventory inventory, boolean shouldRenderBackpack) {




        if (!shouldRenderBackpack) return;

        int columns = 0;
        ItemStack stack = inventory.getStack(36);
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            columns = backpackItem.getBackpackType().getColumns();
        }

        int width = 14 + columns * 18;
        int left = x - 14 - columns * 18 - 1;
        int top = y + (backgroundHeight - 174) / 2;
        int u = 14 * (columns - 1) + 18 * (columns - 1) * columns / 2;

        context.drawTexture(BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);


    }

    // 缩放管理
    public static void adjustScaleIfNeeded(int backgroundWidth, int backgroundHeight, PlayerInventory inventory, 
                                          Ref<Integer> originalGuiScale, Ref<Boolean> wasAutoScale) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;
        Window window = client.getWindow();

        if (originalGuiScale.value != null) return;

        int currentScale = getEffectiveGuiScale(options, window);
        int extendedWidth = getExtendedWidth(inventory);
        int totalWidth = backgroundWidth + extendedWidth;
        int totalHeight = backgroundHeight;
        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();
        int left = (scaledWidth - backgroundWidth) / 2 - extendedWidth;
        int top = (scaledHeight - backgroundHeight) / 2;

        if (left < 0 || top < 0 || (left + totalWidth) > scaledWidth || (top + totalHeight) > scaledHeight) {
            originalGuiScale.value = options.getGuiScale().getValue();
            wasAutoScale.value = (originalGuiScale.value == 0);
            int newScale = Math.max(currentScale - 1, 1);
            options.getGuiScale().setValue(newScale);

            client.execute(() -> {
                if (client.getWindow() != null) {
                    client.onResolutionChanged();
                }
            });
        }
    }

    public static void restoreOriginalScale(Ref<Integer> originalGuiScale, Ref<Boolean> wasAutoScale) {
        if (originalGuiScale.value != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            GameOptions options = client.options;
            options.getGuiScale().setValue(wasAutoScale.value ? 0 : originalGuiScale.value);
            originalGuiScale.value = null;
            wasAutoScale.value = false;

            client.execute(() -> {
                if (client.getWindow() != null) {
                    client.onResolutionChanged();
                }
            });
        }
    }

    private static int getEffectiveGuiScale(GameOptions options, Window window) {
        int settingValue = options.getGuiScale().getValue();
        if (settingValue == 0) {
            int width = window.getWidth();
            int height = window.getHeight();
            int maxScale = 1;
            while (maxScale < 6 && width / (maxScale + 1) >= 320 && height / (maxScale + 1) >= 240) {
                maxScale++;
            }
            return maxScale;
        }
        return settingValue;
    }

    public static int getExtendedWidth(PlayerInventory inventory) {
        if (inventory == null) return 0;
        ItemStack stack = inventory.getStack(36);
        if (stack.getItem() instanceof BackpackItem backpack) {
            return 14 + backpack.getBackpackType().getColumns() * 18;
        }
        return 0;
    }


    public static boolean shouldRenderBackpackExtension(ScreenHandler handler, PlayerInventory inventory) {


        // 检查玩家是否有背包
        if (inventory != null && ((BackpackRenderCondition)handler).shouldRenderBackpack()) {

            ItemStack backpackStack = inventory.getStack(36);

            return backpackStack.getItem() instanceof BackpackItem;
        }

        return false;
    }


    public static boolean isClickOutsideExtendedBounds(PlayerInventory playerInventory, boolean outsideOriginalBounds, double mouseX, double mouseY, int left, int top, int backgroundWidth, int backgroundHeight, boolean shouldRenderBackpackExtension) {

        // 扩展区域检查
        boolean inBackpackArea = false;
        boolean inAdditionalInventoryArea = false;

        if (shouldRenderBackpackExtension) {
            // 获取背包信息
            int columns = 0;
            ItemStack backpackStack = playerInventory.getStack(36);
            if (backpackStack.getItem() instanceof BackpackItem backpack) {
                columns = backpack.getBackpackType().getColumns();
            }

            // 背包扩展区域
            int backpackWidth = 14 + columns * 18;
            int backpackX = left - backpackWidth - 1;
            int backpackY = top + (backgroundHeight - 174) / 2;
            int backpackHeight = 174;

            inBackpackArea = mouseX >= backpackX &&
                    mouseX < backpackX + backpackWidth &&
                    mouseY >= backpackY &&
                    mouseY < backpackY + backpackHeight;


        }

        // 原始边界外且不在扩展区域内

        return outsideOriginalBounds && !inBackpackArea && !inAdditionalInventoryArea;
    }

    public static class Ref<T> {
        public T value;
        public Ref(T value) {
            this.value = value;
        }
    }
}