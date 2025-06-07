package com.yyz.yyzsbackpack.mixin;

import com.google.common.collect.ImmutableList;
import com.yyz.yyzsbackpack.YyzsBackpack;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeScreenHandlerMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    @Shadow private static ItemGroup selectedTab;

    @Shadow @Nullable private List<Slot> slots;

    @Shadow @Nullable private Slot deleteItemSlot;

    @Shadow @Final private static SimpleInventory INVENTORY;

    @Shadow private TextFieldWidget searchBox;

    @Shadow private float scrollPosition;

    @Shadow protected abstract void search();

    public CreativeScreenHandlerMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "setSelectedTab", at = @At("RETURN"))
    private void addMoreRows(ItemGroup group, CallbackInfo ci) {

        ItemGroup itemGroup = selectedTab;
        selectedTab = group;
        this.cursorDragSlots.clear();
        ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.clear();
        this.endTouchDrag();
        if (selectedTab.getType() == ItemGroup.Type.HOTBAR) {
            HotbarStorage hotbarStorage = this.client.getCreativeHotbarStorage();

            for(int i = 0; i < 9; ++i) {
                HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(i);
                if (hotbarStorageEntry.isEmpty()) {
                    for(int j = 0; j < 9; ++j) {
                        if (j == i) {
                            ItemStack itemStack = new ItemStack(Items.PAPER);
                            itemStack.getOrCreateSubNbt("CustomCreativeLock");
                            Text text = this.client.options.hotbarKeys[i].getBoundKeyLocalizedText();
                            Text text2 = this.client.options.saveToolbarActivatorKey.getBoundKeyLocalizedText();
                            itemStack.setCustomName(Text.translatable("inventory.hotbarInfo", new Object[]{text2, text}));
                            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.add(itemStack);
                        } else {
                            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.add(ItemStack.EMPTY);
                        }
                    }
                } else {
                    ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.addAll(hotbarStorageEntry);
                }
            }
        } else if (selectedTab.getType() == ItemGroup.Type.CATEGORY) {
            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).itemList.addAll(selectedTab.getDisplayStacks());
        }

        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            ScreenHandler screenHandler = this.client.player.playerScreenHandler;
            if (this.slots == null) {
                this.slots = ImmutableList.copyOf(((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots);
            }

            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.clear();

            for(int i = 0; i < screenHandler.slots.size(); ++i) {
                int n;
                int j;
                if (i == 94) {
                    // [新增] 背包槽位放在副手槽右侧
                    n = 53; // 副手槽X(35) + 18 = 53
                    j = 20; // 与副手槽Y相同
                }else if (i >= 36 && i <= 89) {
                    // [新增] 背包内容槽位放在左侧
                    int contentIndex = i - 36;
                    int row = contentIndex / 9;
                    int col = contentIndex % 9;
                    n = 8 + col * 18;
                    j = 18 + row * 18;
                }else
                if (i >= 5 && i < 9) {
                    int k = i - 5;
                    int l = k / 2;
                    int m = k % 2;
                    n = 54 + l * 54;
                    j = 6 + m * 27;
                } else if (i >= 0 && i < 5) {
                    n = -2000;
                    j = -2000;
                } else if (i == 45) {
                    n = 35;
                    j = 20;
                } else {
                    int k = i - 9;
                    int l = k % 9;
                    int m = k / 9;
                    n = 9 + l * 18;
                    if (i >= 36) {
                        j = 112;
                    } else {
                        j = 54 + m * 18;
                    }
                }

                Slot slot = new CreativeInventoryScreen.CreativeSlot((Slot)screenHandler.slots.get(i), i, n, j);
                ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.add(slot);
            }

            this.deleteItemSlot = new Slot(INVENTORY, 0, 173, 112);
            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.add(this.deleteItemSlot);
        } else if (itemGroup.getType() == ItemGroup.Type.INVENTORY) {
            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.clear();
            ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).slots.addAll(this.slots);
            this.slots = null;
        }

        if (selectedTab.getType() == ItemGroup.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setFocusUnlocked(false);
            this.searchBox.setFocused(true);
            if (itemGroup != group) {
                this.searchBox.setText("");
            }

            this.search();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setFocusUnlocked(true);
            this.searchBox.setFocused(false);
            this.searchBox.setText("");
        }

        this.scrollPosition = 0.0F;
        ((CreativeInventoryScreen.CreativeScreenHandler)this.handler).scrollItems(0.0F);
    }



}