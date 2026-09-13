package com.yyz.yyzsbackpack.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BackpackCoordEditScreen extends Screen {

    /** 编辑模式，决定每行的描述文字。 */
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

            // ==== 描述文字（可能为 null，null 则不画）====
            final Component labelComp = labelFor(index);
            if (labelComp != null) {
                final int labelX = startX;
                final int labelY = y + 6;
                this.addRenderableOnly(new Renderable() {
                    @Override
                    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                        graphics.drawString(BackpackCoordEditScreen.this.font,
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
        this.addRenderableWidget(Button.builder(
                Component.translatable("yyzsbackpack.config.add_pos"), b -> {
                    collectValues();
                    this.values.add(new int[]{0, 0});
                    rebuild();
                }).bounds(10, addY, 130, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("yyzsbackpack.config.done"), b -> {
                    collectValues();
                    onSave.accept(this.values);
                    if (this.minecraft != null) this.minecraft.setScreen(parent);
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
            case OFFSET -> index == 0
                    ? Component.translatable("yyzsbackpack.config.group.offset")
                    : null;
            case UI -> index == 0
                    ? Component.translatable("yyzsbackpack.config.group.ui")
                    : null;
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double metal) {
        if (this.lastMaxScroll > 0) {
            this.scrollY += (int) (scrollY * 14);
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, metal);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // super.render 会自动渲染背景 + 所有 renderable（描述 / EditBox / 按钮）
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, COLOR_WHITE);

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