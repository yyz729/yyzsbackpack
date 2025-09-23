package com.yyz.yyzsbackpack;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.yyz.yyzsbackpack.config.BackpackConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.Set;

public class BackpackCommand {
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
                                        .executes(BackpackCommand::setTipKey)))
                        .then(Commands.literal("restricted_items")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("item", StringArgumentType.string())
                                                .executes(BackpackCommand::addItem)))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("item", StringArgumentType.string())
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
}