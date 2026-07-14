package com.yyz.yyzsbackpack.mixin.minecraft.container;

import com.yyz.yyzsbackpack.api.IBackpackScroll;
import com.yyz.yyzsbackpack.api.IBackpackTabScroll;
import com.yyz.yyzsbackpack.api.IBackpackVisible;
import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.data.LayoutOrder;
import com.yyz.yyzsbackpack.api.data.LayoutSegment;
import com.yyz.yyzsbackpack.api.helper.BackpackScreenHelper;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.client.key.BackpackKeyBinding;
import com.yyz.yyzsbackpack.client.gui.widget.control.BackpackSortButton;
import com.yyz.yyzsbackpack.client.gui.widget.layout.BackpackTabWidget;
import com.yyz.yyzsbackpack.data.BackpackData;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.mixin.minecraft.accessor.ScreenAccessor;
import com.yyz.yyzsbackpack.network.packets.data.SwitchBackpackC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements IBackpackVisible, IBackpackScroll, IBackpackTabScroll {

    @Shadow
    protected abstract boolean hasClickedOutside(double mx, double my, int xo, int yo);

    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Unique
    private static boolean backpackVisible = true; // 默认可见

    @Override
    public boolean yyzsbackpack$isBackpackVisible() {
        return backpackVisible;
    }

    @Override
    public void yyzsbackpack$setBackpackVisible(boolean visible) {
        backpackVisible = visible;
    }

    @Unique private List<Integer> segmentScrollOffsets = new ArrayList<>();
    @Unique private List<Integer> segmentMaxScrollOffsets = new ArrayList<>();
    @Unique private boolean scrollInitialized = false;

    @Unique
    private void initScrollData() {
        if (scrollInitialized) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = BackpackScreenHelper.getBackpackData(backpack);
        if (data != null) {
            int count = data.segments().size();
            segmentScrollOffsets = new ArrayList<>(Collections.nCopies(count, 0));
            segmentMaxScrollOffsets = new ArrayList<>(Collections.nCopies(count, 0));
        } else {
            segmentScrollOffsets = new ArrayList<>();
            segmentMaxScrollOffsets = new ArrayList<>();
        }
        scrollInitialized = true;
    }

    @Override
    public int yyzsbackpack$getSegmentCount() {
        initScrollData();
        return segmentScrollOffsets.size();
    }

    @Override
    public int yyzsbackpack$getSegmentScrollOffset(int index) {
        initScrollData();
        if (index < 0 || index >= segmentScrollOffsets.size()) return 0;
        return segmentScrollOffsets.get(index);
    }

    @Override
    public void yyzsbackpack$setSegmentScrollOffset(int index, int offset) {
        initScrollData();
        if (index < 0 || index >= segmentScrollOffsets.size()) return;
        int max = yyzsbackpack$getSegmentMaxScrollOffset(index);
        segmentScrollOffsets.set(index, Math.max(0, Math.min(offset, max)));
    }

    @Override
    public int yyzsbackpack$getSegmentMaxScrollOffset(int index) {
        initScrollData();
        if (index < 0 || index >= segmentMaxScrollOffsets.size()) return 0;
        return segmentMaxScrollOffsets.get(index);
    }

    @Override
    public void yyzsbackpack$setSegmentMaxScrollOffset(int index, int max) {
        initScrollData();
        if (index < 0 || index >= segmentMaxScrollOffsets.size()) return;
        segmentMaxScrollOffsets.set(index, Math.max(0, max));
    }


    @Unique
    private int tabScrollOffset = 0;

    @Override
    public int yyzsbackpack$getTabScrollOffset() {
        return tabScrollOffset;
    }

    @Override
    public void yyzsbackpack$setTabScrollOffset(int offset) {
        this.tabScrollOffset = Math.max(0, offset);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        for (var child : screen.children()) {
            if (child instanceof BackpackSortButton btn) {
                if (btn.isMouseOver(x, y)) {
                    BackpackSortButton.cycleAlgorithm();
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        int segmentIndex = getSegmentAtPosition(x, y);
        if (segmentIndex >= 0) {
            int delta = (int)Math.signum(scrollY);
            int oldOffset = yyzsbackpack$getSegmentScrollOffset(segmentIndex);
            int newOffset = oldOffset - delta;
            yyzsbackpack$setSegmentScrollOffset(segmentIndex, newOffset);
            cir.setReturnValue(true);
        }

        boolean mouseOverTab = screen.children().stream()
                .filter(w -> w instanceof BackpackTabWidget)
                .anyMatch(w -> w.isMouseOver(x, y));
        if (mouseOverTab) {
            int delta = (int)Math.signum(scrollY);
            int oldOffset = yyzsbackpack$getTabScrollOffset();
            int newOffset = oldOffset - delta; // 负方向为左滚，正方向为右滚
            yyzsbackpack$setTabScrollOffset(newOffset);
            // 重建标签
            BackpackScreenHelper.addBackpackTabs(screen);
            cir.setReturnValue(true);
        }
    }

    // 根据鼠标位置确定所在的段索引
    @Unique
    private int getSegmentAtPosition(double mouseX, double mouseY) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return -1;
        ItemStack backpack = BackpackSlotHelper.getSelectedBackpack(player);
        BackpackData data = BackpackScreenHelper.getBackpackData(backpack);
        if (data == null) return -1;

        int offsetX = BackpackScreenHelper.getOffsetX((AbstractContainerScreen<?>) (Object) this);
        int offsetY = BackpackScreenHelper.getOffsetY((AbstractContainerScreen<?>) (Object) this);
        int leftPos = ((ScreenAccessor<?>) this).getLeftPos();
        int topPos = ((ScreenAccessor<?>) this).getTopPos();

        List<LayoutSegment> segments = data.segments();
        for (int i = 0; i < segments.size(); i++) {
            LayoutSegment seg = segments.get(i);
            if (seg.order() == LayoutOrder.CUSTOM) continue;
            if (seg.columns().isEmpty() || seg.rows().isEmpty()) continue;

            int segStartX = leftPos + seg.getEffectiveStartX() + offsetX;
            int segStartY = topPos + seg.getEffectiveStartY() + offsetY;
            int width = seg.columns().get() * 18;
            int height = seg.rows().get() * 18; // 可视区域高度

            // 扩大一点点击区域，方便操作
            Rectangle rect = new Rectangle(segStartX, segStartY, width, height);
            if (rect.contains(mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }
    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDII)Z"
            )
    )
    private boolean redirectMouseClicked(
            AbstractContainerScreen<?> screen,
            double mouseX, double mouseY,
            int leftPos, int topPos
    ) {
        boolean original = hasClickedOutside(mouseX, mouseY, leftPos, topPos);
        if (original) {
            Rectangle bounds = BackpackScreenHelper.getBackpackBackgroundBounds(screen);
            if (bounds != null && bounds.contains(mouseX, mouseY)) {
                return false;   // 点击在背包背景内
            }
        }
        return original;
    }

    @Redirect(
            method = "mouseReleased",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDII)Z"
            )
    )
    private boolean redirectMouseReleased(
            AbstractContainerScreen<?> screen,
            double mouseX, double mouseY,
            int leftPos, int topPos
    ) {
        boolean original = hasClickedOutside(mouseX, mouseY, leftPos, topPos);
        if (original) {
            Rectangle bounds = BackpackScreenHelper.getBackpackBackgroundBounds(screen);
            if (bounds != null && bounds.contains(mouseX, mouseY)) {
                return false;
            }
        }
        return original;
    }

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        // 排序键
        if (BackpackKeyBinding.KEY_SORT.matches(event)) {
            System.out.println("按下了排序键！");
            cir.setReturnValue(true);
            return;
        }

        // 打开键
        if (BackpackKeyBinding.KEY_OPEN.matches(event)) {
            if (hoveredSlot != null) {
                ItemStack stack = hoveredSlot.getItem();
                if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                    Player player = Minecraft.getInstance().player;
                    if (player != null) {
                        // 获取所有背包列表
                        List<ItemStack> allBackpacks = BackpackSlotHelper.getAllBackpackStacks(player);
                        // 匹配索引
                        int index = -1;
                        for (int i = 0; i < allBackpacks.size(); i++) {
                            if (ItemStack.matches(stack, allBackpacks.get(i))) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            // 切换背包
                            if (player.getInventory() instanceof IExtendedInventory extInv) {
                                extInv.yyzsbackpack$switchToBackpack(index);
                            }
                            ClientPlayNetworking.send(new SwitchBackpackC2SPacket(index));

                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
            }
            cir.setReturnValue(true);
            return;
        }
    }
}