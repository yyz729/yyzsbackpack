package com.yyz.yyzsbackpack.client.gui.screen;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.api.enums.ButtonMode;
import com.yyz.yyzsbackpack.config.BackpackControlConfig;
import com.yyz.yyzsbackpack.config.BackpackMainConfig;
import com.yyz.yyzsbackpack.config.BackpackOffsetConfig;
import com.yyz.yyzsbackpack.config.BackpackUiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BackpackConfigScreen extends Screen {

    private enum Tab {
        MAIN("yyzsbackpack.config.tab.main"),
        CONTROL("yyzsbackpack.config.tab.control"),
        OFFSET("yyzsbackpack.config.tab.offset"),
        UI("yyzsbackpack.config.tab.ui");

        final String key;
        Tab(String key) { this.key = key; }
    }

    private static final int TOP_BAR_Y = 28;
    private static final int TOP_BAR_H = 20;
    private static final int CONTENT_TOP = 56;
    private static final int BOTTOM_RESERVED = 36;
    private static final int ROW_HEIGHT = 22;
    private static final int COLS = 2;

    private final Screen parent;
    private final BackpackMainConfig mainConfig;
    private BackpackControlConfig controlConfig;
    private BackpackOffsetConfig offsetConfig;
    private BackpackUiConfig uiConfig;
    private final File cfgRoot;

    private Tab currentTab = Tab.MAIN;
    private File currentFile = null;
    private int scrollY = 0;

    // 滚动条用
    private int lastContentHeight = 0;
    private int lastVisibleHeight = 0;
    private int lastMaxScroll = 0;

    private static final int HEAVY_MIN = 1;
    private static final int HEAVY_MAX = 100;

    private BackpackConfigScreen(Screen parent,
                                 BackpackMainConfig mainConfig,
                                 BackpackControlConfig controlConfig,
                                 BackpackOffsetConfig offsetConfig,
                                 BackpackUiConfig uiConfig,
                                 File cfgRoot) {
        super(Component.translatable("yyzsbackpack.config.title"));
        this.parent = parent;
        this.mainConfig = mainConfig;
        this.controlConfig = controlConfig;
        this.offsetConfig = offsetConfig;
        this.uiConfig = uiConfig;
        this.cfgRoot = cfgRoot;
    }

    public static void open(Screen parent) {
        File root = FMLPaths.CONFIGDIR.get()
                .resolve("yyzsbackpack").toFile();

        BackpackMainConfig main =
                BackpackMainConfig.loadConfig(new File(root, "yyzsbackpack.json"));
        BackpackControlConfig control =
                BackpackControlConfig.loadConfig(new File(root, "control"));
        BackpackOffsetConfig offset =
                BackpackOffsetConfig.loadConfig(new File(root, "offset"));
        BackpackUiConfig ui =
                BackpackUiConfig.loadConfig(new File(root, "ui"));

        Minecraft.getInstance().gui.setScreen(
                new BackpackConfigScreen(parent, main, control, offset, ui, root));
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        this.clearWidgets();

        // ---- 顶部标签栏（动态宽度） ----
        Tab[] tabs = Tab.values();
        int gap = 4;
        int available = this.width - 20;
        int tabWidth = Math.min(80, (available - gap * (tabs.length - 1)) / tabs.length);
        if (tabWidth < 40) tabWidth = 40;
        int totalWidth = tabs.length * tabWidth + (tabs.length - 1) * gap;
        int tabStartX = (this.width - totalWidth) / 2;

        for (int i = 0; i < tabs.length; i++) {
            final Tab tab = tabs[i];
            String label = Component.translatable(tab.key).getString();
//            while (label.length() > 3 && this.font.width(label) > tabWidth - 6) {
//                label = label.substring(0, label.length() - 1);
//            }
            Button btn = Button.builder(Component.literal(label), b -> {
                if (currentTab != tab) {
                    currentTab = tab;
                    currentFile = null;
                    scrollY = 0;
                    rebuild();
                }
            }).bounds(tabStartX + i * (tabWidth + gap), TOP_BAR_Y, tabWidth, TOP_BAR_H).build();
            btn.active = currentTab != tab;
            this.addRenderableWidget(btn);
        }

        // ---- 内容区 ----
        if (currentTab == Tab.MAIN) {
            buildMainTab();
        } else if (currentFile == null) {
            buildFileListTab();
        } else {
            buildKeyListTab();
        }

        // ---- 底部按钮（统一 Y 位置，不重叠） ----
        int bottomY = this.height - 28;

        if (currentTab != Tab.MAIN && currentFile != null) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("yyzsbackpack.config.back"), b -> {
                        currentFile = null;
                        scrollY = 0;
                        rebuild();
                    }).bounds(10, bottomY, 70, 20).build());
        }

        if (currentTab != Tab.MAIN) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("yyzsbackpack.config.refresh"), b -> {
                        doReload();
                        rebuild();
                    }).bounds(this.width - 80, bottomY, 70, 20).build());
        }

        this.addRenderableWidget(Button.builder(
                Component.translatable("yyzsbackpack.config.done"), b -> {
                    saveAll();
                    if (this.minecraft != null) this.minecraft.gui.setScreen(parent);
                }).bounds(this.width / 2 - 60, bottomY, 120, 20).build());
    }

    // ==================== Main 页 ====================
    private void buildMainTab() {
        int centerX = this.width / 2;
        int w = 220;
        int y = 80;

        this.addRenderableWidget(Button.builder(
                modelLabel(), b -> {
                    mainConfig.model = !mainConfig.model;
                    b.setMessage(modelLabel());
                }).bounds(centerX - w / 2, y, w, 20).build());
        y += 28;

        // 先夹取一次，防止配置文件里的值超出范围
        if (mainConfig.heavy < HEAVY_MIN) mainConfig.heavy = HEAVY_MIN;
        if (mainConfig.heavy > HEAVY_MAX) mainConfig.heavy = HEAVY_MAX;

        AbstractSliderButton heavy = new AbstractSliderButton(
                centerX - w / 2, y, w, 20,
                Component.translatable("yyzsbackpack.config.heavy", mainConfig.heavy),
                (double)(mainConfig.heavy - HEAVY_MIN) / (HEAVY_MAX - HEAVY_MIN)) {
            @Override
            protected void updateMessage() {
                int v = HEAVY_MIN + (int) Math.round(this.value * (HEAVY_MAX - HEAVY_MIN));
                this.setMessage(Component.translatable("yyzsbackpack.config.heavy", v));
            }

            @Override
            protected void applyValue() {
                mainConfig.heavy = HEAVY_MIN + (int) Math.round(this.value * (HEAVY_MAX - HEAVY_MIN));
            }
        };
        this.addRenderableWidget(heavy);

        y += 28;

        this.addRenderableWidget(Button.builder(
                buttonModeLabel(), b -> {
                    ButtonMode[] modes = ButtonMode.values();
                    mainConfig.button = modes[(mainConfig.button.ordinal() + 1) % modes.length];
                    b.setMessage(buttonModeLabel());
                }).bounds(centerX - w / 2, y, w, 20).build());
    }

    private Component modelLabel() {
        Component state = Component.translatable(mainConfig.model
                ? "yyzsbackpack.config.on" : "yyzsbackpack.config.off");
        return Component.translatable("yyzsbackpack.config.model", state);
    }

    private Component buttonModeLabel() {
        Component mode = Component.translatable(
                "yyzsbackpack.config.button_mode." + mainConfig.button.name().toLowerCase());
        return Component.translatable("yyzsbackpack.config.button_mode", mode);
    }

    // ==================== 文件列表 ====================
    private void buildFileListTab() {
        List<File> files = currentFiles();
        int baseY = CONTENT_TOP;
        int visibleH = this.height - CONTENT_TOP - BOTTOM_RESERVED;
        this.lastVisibleHeight = Math.max(0, visibleH);

        if (files.isEmpty()) {
            this.lastContentHeight = 0;
            this.lastMaxScroll = 0;
            return;
        }

        int colWidth = Math.min(280, (this.width - 40) / COLS);
        int gap = 10;
        int totalWidth = COLS * colWidth + (COLS - 1) * gap;
        int startX = (this.width - totalWidth) / 2;

        int rows = (files.size() + COLS - 1) / COLS;
        this.lastContentHeight = rows * ROW_HEIGHT;
        int maxScroll = Math.max(0, this.lastContentHeight - visibleH);
        this.lastMaxScroll = maxScroll;
        if (this.scrollY < -maxScroll) this.scrollY = -maxScroll;
        if (this.scrollY > 0) this.scrollY = 0;
        this.scrollY = Math.floorDiv(this.scrollY, ROW_HEIGHT) * ROW_HEIGHT;

        int startY = baseY + this.scrollY;

        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (colWidth + gap);
            int y = startY + row * ROW_HEIGHT;

            if (y < baseY || y + 20 > this.height - BOTTOM_RESERVED) continue;

            final File file = f;
            this.addRenderableWidget(Button.builder(Component.literal(f.getName()), b -> {
                currentFile = file;
                scrollY = 0;
                rebuild();
            }).bounds(x, y, colWidth, 20).build());
        }
    }

    // ==================== 单个文件里的 key 列表 ====================
    private void buildKeyListTab() {
        List<String> keys = currentKeys();
        int baseY = CONTENT_TOP;
        int visibleH = this.height - CONTENT_TOP - BOTTOM_RESERVED;
        this.lastVisibleHeight = Math.max(0, visibleH);

        if (keys.isEmpty()) {
            this.lastContentHeight = 0;
            this.lastMaxScroll = 0;
            return;
        }

        int colWidth = Math.min(240, (this.width - 40) / COLS);
        int gap = 10;
        int totalWidth = COLS * colWidth + (COLS - 1) * gap;
        int startX = (this.width - totalWidth) / 2;

        int rows = (keys.size() + COLS - 1) / COLS;
        this.lastContentHeight = rows * ROW_HEIGHT;
        int maxScroll = Math.max(0, this.lastContentHeight - visibleH);
        this.lastMaxScroll = maxScroll;
        if (this.scrollY < -maxScroll) this.scrollY = -maxScroll;
        if (this.scrollY > 0) this.scrollY = 0;
        this.scrollY = Math.floorDiv(this.scrollY, ROW_HEIGHT) * ROW_HEIGHT;

        int startY = baseY + this.scrollY;

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (colWidth + gap);
            int y = startY + row * ROW_HEIGHT;

            if (y < baseY || y + 20 > this.height - BOTTOM_RESERVED) continue;

            List<int[]> valueList = currentValue(key);
            String label = key;
            if (valueList != null && !valueList.isEmpty()) {
                int[] p = valueList.get(0);
                label = key + " (" + p[0] + "," + p[1] + ")"
                        + (valueList.size() > 1 ? " +" + (valueList.size() - 1) : "");
            }
            while (label.length() > 3 && this.font.width(label) > colWidth - 10) {
                label = label.substring(0, label.length() - 1);
            }

            final String keyName = key;
            this.addRenderableWidget(Button.builder(Component.literal(label),
                            b -> openEditor(keyName))
                    .bounds(x, y, colWidth, 20).build());
        }
    }

    // ==================== 数据访问 ====================
    private List<File> currentFiles() {
        return switch (currentTab) {
            case CONTROL -> controlConfig != null ? controlConfig.getAllFiles() : List.of();
            case OFFSET  -> offsetConfig != null ? offsetConfig.getAllFiles() : List.of();
            case UI      -> uiConfig != null ? uiConfig.getAllFiles() : List.of();
            default -> List.of();
        };
    }

    private List<String> currentKeys() {
        if (currentFile == null) return List.of();
        return switch (currentTab) {
            case CONTROL -> controlConfig != null ? controlConfig.getKeysFromFile(currentFile) : List.of();
            case OFFSET  -> offsetConfig != null ? offsetConfig.getKeysFromFile(currentFile) : List.of();
            case UI      -> uiConfig != null ? uiConfig.getKeysFromFile(currentFile) : List.of();
            default -> List.of();
        };
    }

    private List<int[]> currentValue(String key) {
        if (currentFile == null) return new java.util.ArrayList<>();
        return switch (currentTab) {
            case CONTROL -> controlConfig != null ? controlConfig.getValueFromFile(currentFile, key) : new java.util.ArrayList<>();
            case OFFSET  -> offsetConfig != null ? offsetConfig.getValueFromFile(currentFile, key) : new java.util.ArrayList<>();
            case UI      -> uiConfig != null ? uiConfig.getValueFromFile(currentFile, key) : new java.util.ArrayList<>();
            default -> new java.util.ArrayList<>();
        };
    }

    private void saveKeyToCurrentFile(String key, List<int[]> values) throws IOException {
        if (currentFile == null) return;
        switch (currentTab) {
            case CONTROL -> controlConfig.saveKeyTo(key, values, currentFile);
            case OFFSET  -> offsetConfig.saveKeyTo(key, values, currentFile);
            case UI      -> uiConfig.saveKeyTo(key, values, currentFile);
            default -> { }
        }
    }

    private void openEditor(String key) {
        List<int[]> copy = currentValue(key);
        if (this.minecraft == null) return;

        BackpackCoordEditScreen.EditMode mode = switch (currentTab) {
            case CONTROL -> BackpackCoordEditScreen.EditMode.CONTROL;
            case OFFSET  -> BackpackCoordEditScreen.EditMode.OFFSET;
            case UI      -> BackpackCoordEditScreen.EditMode.UI;
            default      -> BackpackCoordEditScreen.EditMode.CONTROL;
        };

        this.minecraft.gui.setScreen(new BackpackCoordEditScreen(
                this, key, copy, mode,
                updated -> {
                    try {
                        saveKeyToCurrentFile(key, updated);
                    } catch (IOException e) {
                        Backpack.LOGGER.error("Failed to save {} -> {}", key, currentFile, e);
                    }
                    doReload();
                }));
    }

    private void doReload() {
        BackpackMainConfig.reload();
        this.controlConfig = BackpackControlConfig.reload();
        this.offsetConfig  = BackpackOffsetConfig.reload();
        this.uiConfig      = BackpackUiConfig.reload();
        Backpack.LOGGER.info("Backpack config reloaded");
    }

    private void saveAll() {
        try {
            mainConfig.saveConfig(new File(cfgRoot, "yyzsbackpack.json"));
        } catch (Exception e) {
            Backpack.LOGGER.error("Failed to save main config", e);
        }
    }

    // ==================== 滚动 ====================
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double scrollX, double scrollY) {
        if (currentTab != Tab.MAIN) {
            this.scrollY += (int) Math.signum(scrollY) * ROW_HEIGHT;
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        saveAll();
        if (this.minecraft != null) this.minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics,
                                   int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        if (currentTab != Tab.MAIN && currentFile != null) {
            graphics.text(this.font, currentFile.getName(), 10, 10, 0xFFFFFFA0);
        }

        if (currentTab != Tab.MAIN && this.lastMaxScroll > 0) {
            drawScrollbar(graphics);
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int viewTop = CONTENT_TOP;
        int viewBottom = this.height - BOTTOM_RESERVED;
        int visibleH = viewBottom - viewTop;
        if (visibleH <= 0) return;

        int visibleRows = (visibleH - 20) / ROW_HEIGHT + 1;

        int trackH = (visibleRows - 1) * ROW_HEIGHT + 20;
        int totalRows = this.lastContentHeight / ROW_HEIGHT;
        if (totalRows <= visibleRows) return;

        // [16, trackH]
        int thumbH = trackH * visibleRows / totalRows;
        if (thumbH < 16) thumbH = 16;
        if (thumbH > trackH) thumbH = trackH;

        // 可滑动的像素范围
        int range = trackH - thumbH;
        int scrollablePx = (totalRows - visibleRows) * ROW_HEIGHT;

        // 当前滚动量,[0, scrollablePx]
        int scrolled = -this.scrollY;
        if (scrolled < 0) scrolled = 0;
        if (scrolled > scrollablePx) scrolled = scrollablePx;

        int thumbOffset = (int) ((long) scrolled * range / scrollablePx);

        int trackX = this.width - 6;
        int barW = 3;

        // 轨道
        graphics.fill(trackX, viewTop, trackX + barW, viewTop + trackH, 0x30FFFFFF);

        // 偏移量一定在 0..range 内，绝不超过轨道
        int thumbY = viewTop + thumbOffset;
        graphics.fill(trackX, thumbY, trackX + barW, thumbY + thumbH, 0xFFFFFFFF);
    }
}