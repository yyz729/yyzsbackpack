package com.yyz.yyzsbackpack.client.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BackpackCoordEditScreen extends Screen {

    public enum EditMode {
        CONTROL,
        OFFSET,
        UI
    }

    private static final int ROW_HEIGHT = 26;
    private static final int TOP_AREA = 50;
    private static final int BOTTOM_RESERVED = 36;
    private static final int FIELD_WIDTH = 70;
    private static final int LABEL_WIDTH = 130;
    private static final int DEL_WIDTH = 24;

    /** UI 模式最大分段数（每段 2 个条目：目标位置 + 锚点）。 */
    private static final int MAX_UI_SEGMENTS = 10;

    private static final int COLOR_WHITE = 0xFFFFFFFF;

    private final Screen parent;
    private final String keyName;
    private final List<int[]> values;
    private final EditMode mode;
    private final Consumer<List<int[]>> onSave;

    private final List<EditBox> xFields = new ArrayList<>();
    private final List<EditBox> yFields = new ArrayList<>();

    private int scrollY = 0;
    private int lastMaxScroll = 0;

    public BackpackCoordEditScreen(Screen parent, String keyName,
                                   List<int[]> values, EditMode mode,
                                   Consumer<List<int[]>> onSave) {
        super(Component.translatable("yyzsbackpack.config.edit", keyName));
        this.parent = parent;
        this.keyName = keyName;
        this.values = values;
        this.mode = mode;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        this.clearWidgets();
        this.xFields.clear();
        this.yFields.clear();

        int totalWidth = LABEL_WIDTH + FIELD_WIDTH * 2 + DEL_WIDTH + 20;
        int startX = (this.width - totalWidth) / 2;
        int baseY = TOP_AREA;

        int visibleH = this.height - TOP_AREA - BOTTOM_RESERVED;
        int contentH = this.values.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, contentH - visibleH);
        this.lastMaxScroll = maxScroll;
        if (this.scrollY < -maxScroll) this.scrollY = -maxScroll;
        if (this.scrollY > 0) this.scrollY = 0;

        for (int i = 0; i < this.values.size(); i++) {
            int y = baseY + i * ROW_HEIGHT + this.scrollY;
            if (y + 20 < TOP_AREA || y > this.height - BOTTOM_RESERVED) continue;

            int[] pos = this.values.get(i);
            final int index = i;

            final Component labelComp = labelFor(index);
            if (labelComp != null) {
                final int labelX = startX;
                final int labelY = y + 6;
                this.addRenderableOnly(new Renderable() {
                    @Override
                    public void extractRenderState(GuiGraphicsExtractor graphics,
                                                   int mouseX, int mouseY, float partialTick) {
                        graphics.text(BackpackCoordEditScreen.this.font,
                                labelComp, labelX, labelY, COLOR_WHITE, false);
                    }
                });
            }

            EditBox xf = new EditBox(this.font,
                    startX + LABEL_WIDTH, y, FIELD_WIDTH, 20,
                    Component.translatable("yyzsbackpack.config.x"));
            xf.setMaxLength(6);
            xf.setValue(String.valueOf(pos[0]));
            xFields.add(xf);
            this.addRenderableWidget(xf);

            EditBox yf = new EditBox(this.font,
                    startX + LABEL_WIDTH + FIELD_WIDTH + 6, y, FIELD_WIDTH, 20,
                    Component.translatable("yyzsbackpack.config.y"));
            yf.setMaxLength(6);
            yf.setValue(String.valueOf(pos[1]));
            yFields.add(yf);
            this.addRenderableWidget(yf);

            this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
                collectValues();
                if (index < this.values.size()) this.values.remove(index);
                rebuild();
            }).bounds(startX + LABEL_WIDTH + FIELD_WIDTH * 2 + 12, y, DEL_WIDTH, 20).build());
        }

        int addY = this.height - 28;

        boolean isUi = (mode == EditMode.UI);
        // UI 模式：每段占 2 个条目（目标位置 + 锚点），最多 MAX_UI_SEGMENTS 段
        boolean canAdd = !isUi || this.values.size() < MAX_UI_SEGMENTS * 2;

        Component addLabel = isUi
                ? Component.translatable("yyzsbackpack.config.add_anchor")
                : Component.translatable("yyzsbackpack.config.add_pos");

        Button addBtn = Button.builder(addLabel, b -> {
            collectValues();
            this.values.add(new int[]{0, 0});
            rebuild();
        }).bounds(10, addY, 130, 20).build();
        addBtn.active = canAdd;
        this.addRenderableWidget(addBtn);

        this.addRenderableWidget(Button.builder(
                Component.translatable("yyzsbackpack.config.done"), b -> {
                    collectValues();
                    if (mode == EditMode.UI) {
                        // 每一段的锚点百分比钳制到 0~100（index 1, 3, 5, ...）
                        for (int i = 1; i < this.values.size(); i += 2) {
                            int[] a = this.values.get(i);
                            a[0] = Math.max(0, Math.min(100, a[0]));
                            a[1] = Math.max(0, Math.min(100, a[1]));
                        }
                    }
                    onSave.accept(this.values);
                    if (this.minecraft != null) this.minecraft.gui.setScreen(parent);
                }).bounds(this.width / 2 - 60, addY, 120, 20).build());
    }

    private void collectValues() {
        int n = Math.min(this.values.size(),
                Math.min(this.xFields.size(), this.yFields.size()));
        for (int i = 0; i < n; i++) {
            int[] cur = this.values.get(i);
            cur[0] = parseOr(this.xFields.get(i).getValue(), cur[0]);
            cur[1] = parseOr(this.yFields.get(i).getValue(), cur[1]);
        }
    }

    private static int parseOr(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Component labelFor(int index) {
        return switch (mode) {
            case CONTROL -> {
                if (index == 0) yield Component.translatable("yyzsbackpack.config.group.base");
                if (index == 1) yield Component.translatable("yyzsbackpack.config.group.extra");
                yield null;
            }
            case OFFSET -> Component.translatable(
                    "yyzsbackpack.config.group.offset_seg", index);
            case UI -> {
                int seg = index / 2;
                boolean isAnchor = (index % 2) == 1;
                yield isAnchor
                        ? Component.translatable("yyzsbackpack.config.group.ui_anchor_seg", seg)
                        : Component.translatable("yyzsbackpack.config.group.ui_target_seg", seg);
            }
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double scrollX, double scrollY) {
        if (this.lastMaxScroll > 0) {
            this.scrollY += (int) (scrollY * 14);
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics,
                                   int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, this.title, this.width / 2, 20, COLOR_WHITE);

        if (this.lastMaxScroll > 0) {
            int trackX = this.width - 6;
            int trackY = TOP_AREA;
            int trackH = this.height - TOP_AREA - BOTTOM_RESERVED;
            graphics.fill(trackX, trackY, trackX + 3, trackY + trackH, 0x30FFFFFF);

            int contentH = this.values.size() * ROW_HEIGHT;
            int thumbH = Math.max(20, (int) ((long) trackH * trackH / contentH));
            int thumbY = trackY + (int) ((long) (-this.scrollY) * (trackH - thumbH) / this.lastMaxScroll);
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFFFFFFF);
        }
    }
}