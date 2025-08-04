package com.yyz.yyzsbackpack;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BackpackCommand {

    private static final String[] MODIFIER_SUGGESTIONS = new String[]{"shift", "alt", "ctrl", "none"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("yyzsbackpack")
                .requires(source -> source.hasPermission(2)) // 需要OP权限
                .then(Commands.literal("set")
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
                        .then(Commands.literal("reload")
                                .executes(BackpackCommand::reloadConfig))
                )
        );
    }

    private static int setBoolean(CommandContext<CommandSourceStack> ctx, String property) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        setProperty(property, value);
        ctx.getSource().sendSuccess(
            () -> Component.literal("Set " + property + " to " + value), 
            true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int setInt(CommandContext<CommandSourceStack> ctx, String property) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        setProperty(property, value);
        ctx.getSource().sendSuccess(
            () -> Component.literal("Set " + property + " to " + value), 
            true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int setTipKey(CommandContext<CommandSourceStack> ctx) {
        String value = StringArgumentType.getString(ctx, "value");
        if (!Set.of("shift", "alt", "ctrl", "none").contains(value)) {
            ctx.getSource().sendFailure(Component.literal("Invalid value! Must be: shift, alt, ctrl, none"));
            return 0;
        }
        setProperty("tooltip_modifier", value);
        ctx.getSource().sendSuccess(
            () -> Component.literal("Set tooltip_modifier to " + value),
            true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static void setProperty(String name, Object value) {
        BackpackConfig config = Backpack.getConfig();
        try {
            config.getClass().getField(name).set(config, value);
            config.saveConfig(new File(BackpackPlatform.getConfigDirectory() + "/yyzsbackpack.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int addItem(CommandContext<CommandSourceStack> ctx) {
        String item = StringArgumentType.getString(ctx, "item");
        BackpackConfig config = Backpack.getConfig();
        if (config.restricted_items.add(item)) {
            config.saveConfig(new File(BackpackPlatform.getConfigDirectory() + "/yyzsbackpack.json"));
            ctx.getSource().sendSuccess(
                () -> Component.literal("Added item: " + item), 
                true
            );
            return Command.SINGLE_SUCCESS;
        }
        ctx.getSource().sendFailure(Component.literal("Item already exists!"));
        return 0;
    }

    private static int removeItem(CommandContext<CommandSourceStack> ctx) {
        String item = StringArgumentType.getString(ctx, "item");
        BackpackConfig config = Backpack.getConfig();
        if (config.restricted_items.remove(item)) {
            config.saveConfig(new File(BackpackPlatform.getConfigDirectory() + "/yyzsbackpack.json"));
            ctx.getSource().sendSuccess(
                () -> Component.literal("Removed item: " + item), 
                true
            );
            return Command.SINGLE_SUCCESS;
        }
        ctx.getSource().sendFailure(Component.literal("Item not found!"));
        return 0;
    }

    private static int clearItems(CommandContext<CommandSourceStack> ctx) {
        BackpackConfig config = Backpack.getConfig();
        config.restricted_items.clear();
        config.saveConfig(new File(BackpackPlatform.getConfigDirectory() + "/yyzsbackpack.json"));
        ctx.getSource().sendSuccess(
            () -> Component.literal("Cleared all container items"), 
            true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        Backpack.init(); // 重新加载配置
        ctx.getSource().sendSuccess(
            () -> Component.literal("Reloaded backpack config"), 
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

//    // 提供所有注册物品的建议
//    private static CompletableFuture<Suggestions> suggestAllItems(
//            CommandSourceStack source, SuggestionsBuilder builder) {
//        // 获取所有已注册物品
//        Iterable<ResourceLocation> itemIds = BuiltInRegistries.ITEM.keySet();
//
//        // 将物品ID转换为字符串建议
//        return SharedSuggestionProvider.suggestResource(
//                itemIds,
//                builder.createOffset(builder.getStart()));
//    }
//
//    // 提供已配置物品的建议
//    private static CompletableFuture<Suggestions> suggestConfiguredItems(
//            CommandSourceStack source, SuggestionsBuilder builder) {
//        // 获取配置中的限制物品列表
//        Set<String> configuredItems = Backpack.getConfig().restricted_items;
//
//        // 直接建议配置中的物品ID
//        return SharedSuggestionProvider.suggest(
//                configuredItems,
//                builder.createOffset(builder.getStart()));
//    }
}