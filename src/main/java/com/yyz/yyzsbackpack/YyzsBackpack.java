package com.yyz.yyzsbackpack;

import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Unique;


public class YyzsBackpack implements ModInitializer {

    public static final String MOD_ID = "yyzsbackpack";

    @Override
    public void onInitialize() {
        ModItems.register();
    }


    public static void addBackpack(ScreenHandler screenHandler, Inventory inventory, int w , int h, boolean bl){
        int x = w + 9;
        int y = h - 24;
        if(bl) {
            x = 8 + 69;
            y = 8;

        }
        screenHandler.addSlot(new Slot(inventory, 94, x,y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof BackpackItem;
            }
            // 当背包被取下时保存内容物品到NBT
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack backpackStack) {
                if (backpackStack.getItem() instanceof BackpackItem) {
                    saveBackpackContents(inventory, backpackStack);
                }
                super.onTakeItem(player, backpackStack);
            }

            // 当背包被放入时恢复物品
            // 当背包被放入或交换时处理物品
            @Override
            public void setStack(ItemStack newBackpackStack) {
                ItemStack oldBackpackStack = this.getStack();

                // 如果旧背包是背包物品，保存其内容
                if (!oldBackpackStack.isEmpty() && oldBackpackStack.getItem() instanceof BackpackItem) {
                    saveBackpackContents(inventory, oldBackpackStack);
                }

                super.setStack(newBackpackStack);

                // 如果新背包是背包物品，恢复其内容
                if (!newBackpackStack.isEmpty() && newBackpackStack.getItem() instanceof BackpackItem) {
                    restoreBackpackContents(inventory, newBackpackStack);
                }

            }

            @Override
            public boolean isEnabled() {
                // 获取当前屏幕
                Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                // 如果是配方书提供者（如合成表界面），则检查配方书是否打开
                if (currentScreen instanceof RecipeBookProvider) {
                    return !((RecipeBookProvider) currentScreen).getRecipeBookWidget().isOpen();
                }
                // 其他界面（如背包界面）始终启用槽位
                return true;
            }
        });

        // 添加所有可能的槽位
        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 9; row++) {
                // 记录当前槽位的列索引
                final int columnIndex = column;

                screenHandler.addSlot(new Slot(inventory, row + (column + 1) * 9 + 27, -25 - column * 18, (h - 166) / 2 + 3 + row * 18) {
                    @Override
                    public boolean isEnabled() {
                        // 检查94号槽位是否有背包
                        ItemStack backpackStack = inventory.getStack(94);
                        if (!(backpackStack.getItem() instanceof BackpackItem)) {
                            return false;
                        }

                        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
                        int columns = backpackItem.getBackpackType().getColumns();
                        // 如果当前列索引大于等于背包的列数，则禁用槽位
                        if (columnIndex >= columns) {
                            return false;
                        }

                        // 获取当前屏幕
                        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                        // 如果是配方书提供者（如合成表界面），则检查配方书是否打开
                        if (currentScreen instanceof RecipeBookProvider) {
                            return !((RecipeBookProvider) currentScreen).getRecipeBookWidget().isOpen();
                        }
                        // 其他界面（如背包界面）始终启用槽位
                        return true;
                    }
                });
            }
        }
    }
    // 保存背包内容到NBT
    @Unique
    private static void saveBackpackContents(Inventory inventory, ItemStack backpackStack) {
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int columns = backpackItem.getBackpackType().getColumns();
        int numSlots = columns * 9;

        NbtList itemsTag = new NbtList();

        for (int i = 0; i < numSlots; i++) {
            int slotIndex = 36 + i; // 背包内容槽位起始索引
            ItemStack stack = inventory.getStack(slotIndex);

            if (!stack.isEmpty()) {
                NbtCompound itemTag = new NbtCompound();
                itemTag.putInt("Slot", i);
                stack.writeNbt(itemTag);
                itemsTag.add(itemTag);
                inventory.setStack(slotIndex, ItemStack.EMPTY); // 清空槽位
            }
        }

        NbtCompound nbt = backpackStack.getOrCreateNbt();
        nbt.put("BackpackItems", itemsTag);
    }

    // 从NBT恢复背包内容
    @Unique
    private static void restoreBackpackContents(Inventory inventory, ItemStack backpackStack) {
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
                inventory.setStack(36 + slotIndex, stack); // 恢复物品到槽位
            }
        }

        nbt.remove("BackpackItems"); // 移除临时数据
        if (nbt.isEmpty()) {
            backpackStack.setNbt(null);
        }
    }

}
