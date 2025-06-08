package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

public class BackpackManager {
    public static final Identifier BACKPACK_TEXTURE = new Identifier(YyzsBackpack.MOD_ID, "textures/gui/backpack.png");
    public static final Identifier EQUIPMENT_TEXTURE = new Identifier(YyzsBackpack.MOD_ID, "textures/gui/equipment.png");
    public static final List<Class<? extends Screen>> ALLOWED_SCREENS = new ArrayList<>();

    static {
        ALLOWED_SCREENS.add(InventoryScreen.class);
        ALLOWED_SCREENS.add(CreativeInventoryScreen.class);
        ALLOWED_SCREENS.add(GenericContainerScreen.class);
        ALLOWED_SCREENS.add(BeaconScreen.class);
        ALLOWED_SCREENS.add(BrewingStandScreen.class);
        ALLOWED_SCREENS.add(CartographyTableScreen.class);
        ALLOWED_SCREENS.add(CraftingScreen.class);
        ALLOWED_SCREENS.add(EnchantmentScreen.class);
        ALLOWED_SCREENS.add(ForgingScreen.class);
        ALLOWED_SCREENS.add(AbstractFurnaceScreen.class);
        ALLOWED_SCREENS.add(Generic3x3ContainerScreen.class);
        ALLOWED_SCREENS.add(GrindstoneScreen.class);
        ALLOWED_SCREENS.add(HopperScreen.class);
        ALLOWED_SCREENS.add(LoomScreen.class);
        ALLOWED_SCREENS.add(ShulkerBoxScreen.class);
        ALLOWED_SCREENS.add(StonecutterScreen.class);
    }

    // 供其他mod添加允许的屏幕
    public static void addAllowedScreen(Class<? extends Screen> screenClass) {
        if (!ALLOWED_SCREENS.contains(screenClass)) {
            ALLOWED_SCREENS.add(screenClass);
        }
    }

    // 背包槽位管理
    public static void addBackpackSlots(ScreenHandler screenHandler, Inventory inventory, int width, int height, boolean isCreative) {
        int x = width + 9;
        int y = height - 24;
        if (isCreative) {
            x = 8 + 69;
            y = 8;
        }
        
        screenHandler.addSlot(new Slot(inventory, 94, x, y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof BackpackItem;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack backpackStack) {
                if (backpackStack.getItem() instanceof BackpackItem) {
                    saveBackpackContents(inventory, backpackStack);
                }
                super.onTakeItem(player, backpackStack);
            }

            @Override
            public void setStack(ItemStack newBackpackStack) {
                ItemStack oldBackpackStack = this.getStack();
                if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
                    saveBackpackContents(inventory, oldBackpackStack);
                }

                super.setStack(newBackpackStack);

                if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
                    restoreBackpackContents(inventory, newBackpackStack);
                }
            }

            @Override
            public boolean isEnabled() {
                Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                if (currentScreen instanceof RecipeBookProvider) {
                    return !((RecipeBookProvider) currentScreen).getRecipeBookWidget().isOpen();
                }
                return true;
            }
        });

        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 9; row++) {
                final int columnIndex = column;
                screenHandler.addSlot(new Slot(inventory, row + (column + 1) * 9 + 27, -25 - column * 18, (height - 166) / 2 + 3 + row * 18) {
                    @Override
                    public boolean isEnabled() {
                        ItemStack backpackStack = inventory.getStack(94);
                        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
                            return false;
                        }

                        int columns = backpackItem.getBackpackType().getColumns();
                        if (columnIndex >= columns) {
                            return false;
                        }

                        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                        if (currentScreen instanceof RecipeBookProvider) {
                            return !((RecipeBookProvider) currentScreen).getRecipeBookWidget().isOpen();
                        }
                        return true;
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
            int slotIndex = 36 + i;
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
                inventory.setStack(36 + slotIndex, stack);
            }
        }

        nbt.remove("BackpackItems");
        if (nbt.isEmpty()) {
            backpackStack.setNbt(null);
        }
    }

    // GUI 渲染辅助方法
    public static void renderBackpackBackground(DrawContext context, int x, int y, int backgroundWidth, int backgroundHeight,
                                                PlayerInventory inventory, boolean shouldRenderBonusRows,ScreenHandler handler) {

        if (!(handler instanceof PlayerScreenHandler) && !(handler instanceof CreativeInventoryScreen.CreativeScreenHandler)) {
            context.drawTexture(EQUIPMENT_TEXTURE, x + backgroundWidth + 1, y + backgroundHeight - 108, 0, 0, 32, 108, 32, 108);
        }

        if (!shouldRenderBonusRows) return;

        int columns = 0;
        ItemStack stack = inventory.getStack(94);
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            columns = backpackItem.getBackpackType().getColumns();
        }

        int width = 14 + columns * 18;
        int left = x - 14 - columns * 18 - 1;
        int top = y + (backgroundHeight - 174) / 2;
        int u = 14 * (columns - 1) + 18 * (columns - 1) * columns / 2;

        if (width > 0) {
            context.drawTexture(BACKPACK_TEXTURE, left, top, u, 0, width, 174, 462, 174);
        }


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
        ItemStack stack = inventory.getStack(94);
        if (stack.getItem() instanceof BackpackItem backpack) {
            return 14 + backpack.getBackpackType().getColumns() * 18;
        }
        return 0;
    }


    public static boolean shouldRenderBackpackExtension(PlayerInventory playerInventory) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;

        // 检查配方书是否打开
        if (currentScreen instanceof RecipeBookProvider &&
                ((RecipeBookProvider) currentScreen).getRecipeBookWidget().isOpen()) {
            return false;
        }

        // 检查创造模式标签页
        if (currentScreen instanceof CreativeInventoryScreen creativeScreen &&
                !creativeScreen.isInventoryTabSelected()) {
            return false;
        }

        // 检查是否在允许的屏幕列表中
        boolean isAllowedScreen = false;
        for (Class<? extends Screen> screenClass : BackpackManager.ALLOWED_SCREENS) {
            if (screenClass.isInstance(currentScreen)) {
                isAllowedScreen = true;
                break;
            }
        }
        if (!isAllowedScreen) return false;

        // 检查玩家是否有背包
        if (playerInventory != null) {
            ItemStack backpackStack = playerInventory.getStack(94);
            return backpackStack.getItem() instanceof BackpackItem;
        }

        return false;
    }

    @Unique
    public static boolean isClickOutsideExtendedBounds(PlayerInventory playerInventory, double mouseX, double mouseY, int left, int top, int button, int backgroundWidth, int backgroundHeight, boolean shouldRenderBackpackExtension, int x, int y) {
        // 原始GUI边界检查
        boolean outsideOriginalBounds = mouseX < (double)left || mouseY < (double)top ||
                mouseX >= (double)(left + backgroundWidth) ||
                mouseY >= (double)(top + backgroundHeight);

        // 扩展区域检查
        boolean inBackpackArea = false;
        boolean inAdditionalInventoryArea = false;

        if (shouldRenderBackpackExtension) {
            // 获取背包信息
            int columns = 0;
            ItemStack backpackStack = playerInventory.getStack(94);
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

            // 额外物品栏区域
            int inventoryWidth = 32;
            int inventoryHeight = 108;
            int inventoryX = left + backgroundWidth + 1;
            int inventoryY = top + backgroundHeight - inventoryHeight;

            inAdditionalInventoryArea = mouseX >= inventoryX &&
                    mouseX < inventoryX + inventoryWidth &&
                    mouseY >= inventoryY &&
                    mouseY < inventoryY + inventoryHeight;
        }

        // 最终判断：原始边界外且不在扩展区域内
        boolean outsideExtendedBounds = outsideOriginalBounds && !inBackpackArea && !inAdditionalInventoryArea;

        // 处理配方书特殊情况
        if (MinecraftClient.getInstance().currentScreen != null &&
                MinecraftClient.getInstance().currentScreen instanceof RecipeBookProvider provider) {

            boolean recipeBookClick = provider.getRecipeBookWidget().isClickOutsideBounds(
                    mouseX, mouseY, x, y,
                    backgroundWidth, backgroundHeight, button
            );

            return recipeBookClick && outsideExtendedBounds;
        } else {
            return outsideExtendedBounds;
        }
    }

    // 引用包装类
    public static class Ref<T> {
        public T value;
        public Ref(T value) {
            this.value = value;
        }
    }
}