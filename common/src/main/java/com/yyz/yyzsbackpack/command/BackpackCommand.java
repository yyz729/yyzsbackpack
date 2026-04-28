package com.yyz.yyzsbackpack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackupRecord;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import com.yyz.yyzsbackpack.base.BackpackEffect;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.util.BackpackBackup;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BackpackCommand {

    private static final String[] MODIFIER_SUGGESTIONS = new String[]{"shift", "alt", "ctrl", "none"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("yyzsbackpack")
                .requires(source -> source.hasPermission(2)) // 需要OP权限
                .then(Commands.literal("config")
                        .then(Commands.literal("quick_swap_backpack")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setBoolean(ctx, "quick_swap_backpack"))))
                        .then(Commands.literal("use_dedicated_slot")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setBoolean(ctx, "use_dedicated_slot"))))
                        .then(Commands.literal("render_backpack_model")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setBoolean(ctx, "render_backpack_model"))))
                        .then(Commands.literal("restrict_container_items")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setBoolean(ctx, "restrict_container_items"))))
                        .then(Commands.literal("slot_position_x")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> setInt(ctx, "slot_position_x"))))
                        .then(Commands.literal("slot_position_y")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> setInt(ctx, "slot_position_y"))))
                        .then(Commands.literal("backpack_gui_x")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> setInt(ctx, "backpack_gui_x"))))
                        .then(Commands.literal("backpack_gui_y")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> setInt(ctx, "backpack_gui_y"))))
                        .then(Commands.literal("tooltip_modifier")
                                .then(Commands.argument("value", StringArgumentType.string())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(MODIFIER_SUGGESTIONS, builder))
                                        .executes(BackpackCommand::setTipKey)))
                        .then(Commands.literal("restricted_items")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("item", StringArgumentType.string())
                                                // 添加物品建议
                                                .suggests((context, builder) ->
                                                        suggestAllItems(context.getSource(), builder))
                                                .executes(BackpackCommand::addItem)))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("item", StringArgumentType.string())
                                                // 添加已配置物品建议
                                                .suggests((context, builder) ->
                                                        suggestConfiguredItems(context.getSource(), builder))
                                                .executes(BackpackCommand::removeItem)))
                                .then(Commands.literal("clear")
                                        .executes(BackpackCommand::clearItems)))
                        .then(Commands.literal("backpack_multi_effects")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("effect", StringArgumentType.string())
                                                        .suggests(BackpackCommand::suggestEffectTypes)
                                                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                                                .executes(BackpackCommand::setEffect)
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("effect", StringArgumentType.string())
                                                .suggests(BackpackCommand::suggestEffectTypes)
                                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                                        .executes(BackpackCommand::addEffect)
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(0))
                                                .suggests(BackpackCommand::suggestEffectTiers)
                                                .executes(BackpackCommand::removeEffect)
                                        ))
                                .then(Commands.literal("clear")
                                        .executes(BackpackCommand::clearEffects)
                                )

                        )
                        .then(Commands.literal("reload")
                                .executes(BackpackCommand::reloadConfig))
                )
                .then(Commands.literal("backup")
                        .then(Commands.literal("now")
                                .executes(BackpackCommand::backupNow))
                        .then(Commands.literal("list")
                                .executes(BackpackCommand::listBackups))
                        .then(Commands.literal("restore")
                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                        .executes(BackpackCommand::restoreBackup)))
                        .then(Commands.literal("delete")
                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                        .executes(BackpackCommand::deleteBackup)))
                        .then(Commands.literal("set")
                                .then(Commands.literal("interval")
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                                                .executes(BackpackCommand::setBackupInterval)))
                                .then(Commands.literal("max")
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                .executes(BackpackCommand::setMaxBackups))))
                )
        );
    }

    private static int setBoolean(CommandContext<CommandSourceStack> ctx, String property) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        setProperty(property, value);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.set.boolean", property, String.valueOf(value)),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int setInt(CommandContext<CommandSourceStack> ctx, String property) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        setProperty(property, value);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.set.integer", property, value),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int setTipKey(CommandContext<CommandSourceStack> ctx) {
        String value = StringArgumentType.getString(ctx, "value");
        if (!Set.of("shift", "alt", "ctrl", "none").contains(value)) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.set.tooltip_modifier.invalid"));
            return 0;
        }
        setProperty("tooltip_modifier", value);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.set.tooltip_modifier", value),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static void setProperty(String name, Object value) {
        BackpackConfig config = Backpack.getConfig();
        try {
            config.getClass().getField(name).set(config, value);
            config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int addItem(CommandContext<CommandSourceStack> ctx) {
        String item = StringArgumentType.getString(ctx, "item");
        BackpackConfig config = Backpack.getConfig();
        if (config.restricted_items.add(item)) {
            config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.yyzsbackpack.restricted_items.add", item),
                    true
            );
            return Command.SINGLE_SUCCESS;
        }
        ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.restricted_items.exists"));
        return 0;
    }

    private static int removeItem(CommandContext<CommandSourceStack> ctx) {
        String item = StringArgumentType.getString(ctx, "item");
        BackpackConfig config = Backpack.getConfig();
        if (config.restricted_items.remove(item)) {
            config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.yyzsbackpack.restricted_items.remove", item),
                    true
            );
            return Command.SINGLE_SUCCESS;
        }
        ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.restricted_items.not_found"));
        return 0;
    }

    private static int clearItems(CommandContext<CommandSourceStack> ctx) {
        BackpackConfig config = Backpack.getConfig();
        config.restricted_items.clear();
        config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.restricted_items.clear"),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        Backpack.init(); // 重新加载配置
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.reload"),
                true
        );
        return Command.SINGLE_SUCCESS;
    }


    // 提供所有注册物品的建议
    private static CompletableFuture<Suggestions> suggestAllItems(
            CommandSourceStack source, SuggestionsBuilder builder) {
        // 获取所有已注册物品ID
        Iterable<ResourceLocation> itemIds = BuiltInRegistries.ITEM.keySet();

        // 创建带引号的建议列表
        List<String> quotedSuggestions = new ArrayList<>();
        for (ResourceLocation id : itemIds) {
            quotedSuggestions.add("\"" + id.toString() + "\"");
        }

        return SharedSuggestionProvider.suggest(
                quotedSuggestions,
                builder.createOffset(builder.getStart()));
    }

    // 提供已配置物品的建议
    private static CompletableFuture<Suggestions> suggestConfiguredItems(
            CommandSourceStack source, SuggestionsBuilder builder) {
        // 获取配置中的限制物品列表
        Set<String> configuredItems = Backpack.getConfig().restricted_items;

        // 创建带引号的建议列表
        List<String> quotedSuggestions = new ArrayList<>();
        for (String item : configuredItems) {
            quotedSuggestions.add("\"" + item + "\"");
        }

        return SharedSuggestionProvider.suggest(
                quotedSuggestions,
                builder.createOffset(builder.getStart()));
    }

    private static int setEffect(CommandContext<CommandSourceStack> ctx) {
        int tier = IntegerArgumentType.getInteger(ctx, "tier");
        String effect = StringArgumentType.getString(ctx, "effect");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        BackpackConfig config = Backpack.getConfig();
        List<BackpackEffect> effects = config.backpack_multi_effects;

        // 扩展列表到所需层级
        while (effects.size() <= tier) {
            effects.add(new BackpackEffect("none", 0));
        }

        // 设置效果
        BackpackEffect be = effects.get(tier);
        be.effectType = effect;
        be.amplifier = level;

        config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.effect.set", tier, effect, level),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int addEffect(CommandContext<CommandSourceStack> ctx) {
        String effect = StringArgumentType.getString(ctx, "effect");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        BackpackConfig config = Backpack.getConfig();
        config.backpack_multi_effects.add(new BackpackEffect(effect, level));

        config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.effect.add", effect, level),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int removeEffect(CommandContext<CommandSourceStack> ctx) {
        int tier = IntegerArgumentType.getInteger(ctx, "tier");
        BackpackConfig config = Backpack.getConfig();
        List<BackpackEffect> effects = config.backpack_multi_effects;

        if (tier >= effects.size()) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.effect.invalid_tier", effects.size() - 1));
            return 0;
        }

        effects.remove(tier);
        config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.effect.remove", tier),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int clearEffects(CommandContext<CommandSourceStack> ctx) {
        BackpackConfig config = Backpack.getConfig();
        config.backpack_multi_effects.clear();
        config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.yyzsbackpack.effect.clear"),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> suggestEffectTypes(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("\"none\"");

        // 获取所有注册的效果ID，并添加引号
        BuiltInRegistries.MOB_EFFECT.keySet().forEach(id ->
                suggestions.add("\"" + id.toString() + "\"")
        );

        return SharedSuggestionProvider.suggest(suggestions, builder);
    }

    private static CompletableFuture<Suggestions> suggestEffectTiers(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        BackpackConfig config = Backpack.getConfig();
        List<String> tiers = new ArrayList<>();

        for (int i = 0; i < config.backpack_multi_effects.size(); i++) {
            tiers.add(String.valueOf(i));
        }

        return SharedSuggestionProvider.suggest(tiers, builder);
    }

    private static int backupNow(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        ItemStack backpack = BackpackPlatform.getEquipped(player);
        if (!(backpack.getItem() instanceof BackpackItem)) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.backup.no_backpack"));
            return 0;
        }
        // 立即备份
        BackpackBackup.backupBackpackContents(backpack, BackpackPlatform.getContainer(player),
                Backpack.getConfig().max_backup_count);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.yyzsbackpack.backup.success"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listBackups(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.only_player"));
            return 0;
        }
        ItemStack backpack = BackpackPlatform.getEquipped(player);
        if (!(backpack.getItem() instanceof BackpackItem)) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.backup.no_backpack"));
            return 0;
        }
        List<BackupRecord> backups = backpack.get(BackpackPlatform.getBackupRecordsComponent());
        if (backups == null || backups.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.backup.list.empty"));
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ctx.getSource().sendSuccess(() -> Component.translatable("command.yyzsbackpack.backup.list.header"), false);

        for (int i = 0; i < backups.size(); i++) {
            BackupRecord record = backups.get(i);
            String timeStr = sdf.format(new Date(record.timestamp()));
            List<ItemStack> items = record.items();
            int itemCount = (int) items.stream().filter(s -> !s.isEmpty()).count();

            // 构建预览文本（悬停显示）
            Component preview = buildPreviewComponent(items);

            // 构建可点击的数字（例如 [0]）
            int finalI = i;
            Component clickableNumber = Component.literal("[" + i + "]")
                    .withStyle(style -> style
                            .withColor(net.minecraft.ChatFormatting.GOLD)
                            .withClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/yyzsbackpack backup restore " + finalI
                            ))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("command.yyzsbackpack.backup.list.click_to_restore")
                                            .append("\n")
                                            .append(preview)
                            ))
                    );

            // 整体行： [0] 2026-04-27 12:34:56 (12 items)
            Component line = Component.literal("")
                    .append(clickableNumber)
                    .append(Component.literal(" §f" + timeStr + " §7(" + itemCount + " items)"));

            ctx.getSource().sendSuccess(() -> line, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Component buildPreviewComponent(List<ItemStack> items) {
        MutableComponent preview = Component.literal("");
        int nonEmptyCount = 0;
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            nonEmptyCount++;
            if (nonEmptyCount > 1) preview.append(Component.literal("\n"));
            // 格式: "铁锭 x 3" 或 "钻石剑 (锋利V)"
            MutableComponent itemLine = stack.getHoverName().copy();
            if (stack.getCount() > 1) {
                itemLine.append(Component.literal(" x " + stack.getCount()).withStyle(ChatFormatting.GRAY));
            }
            preview.append(itemLine);
        }
        if (nonEmptyCount == 0) {
            preview = Component.translatable("command.yyzsbackpack.backup.list.empty_preview");
        }
        return preview;
    }

    private static int restoreBackup(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        ItemStack backpack = BackpackPlatform.getEquipped(player);
        if (!(backpack.getItem() instanceof BackpackItem)) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.backup.no_backpack"));
            return 0;
        }
        int index = IntegerArgumentType.getInteger(ctx, "index");
        boolean success = BackpackBackup.restoreBackup(backpack, player.getInventory(), index);
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.yyzsbackpack.backup.restore.success", index), true);
        } else {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.backup.restore.failed", index));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteBackup(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        ItemStack backpack = BackpackPlatform.getEquipped(player);
        if (!(backpack.getItem() instanceof BackpackItem)) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.backup.no_backpack"));
            return 0;
        }
        int index = IntegerArgumentType.getInteger(ctx, "index");
        List<BackupRecord> backups = backpack.get(BackpackPlatform.getBackupRecordsComponent());
        if (backups == null || index < 0 || index >= backups.size()) {
            ctx.getSource().sendFailure(Component.translatable("command.yyzsbackpack.backup.delete.invalid_index"));
            return 0;
        }
        List<BackupRecord> newList = new ArrayList<>(backups);
        newList.remove(index);
        backpack.set(BackpackPlatform.getBackupRecordsComponent(), newList);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.yyzsbackpack.backup.delete.success", index), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setBackupInterval(CommandContext<CommandSourceStack> ctx) {
        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
        BackpackConfig config = Backpack.getConfig();
        config.backup_interval_seconds = seconds;
        config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(() -> Component.translatable("command.yyzsbackpack.backup.set.interval", seconds), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setMaxBackups(CommandContext<CommandSourceStack> ctx) {
        int count = IntegerArgumentType.getInteger(ctx, "count");
        BackpackConfig config = Backpack.getConfig();
        config.max_backup_count = count;
        config.saveConfig(new File(BackpackPlatform.getConfigDirectory().resolve("yyzsbackpack") + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(() -> Component.translatable("command.yyzsbackpack.backup.set.max", count), true);
        return Command.SINGLE_SUCCESS;
    }
}