package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DebugRenderersCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("debug_renderers")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("navigation")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(DebugRenderersCommand::navigation)))
                .then(Commands.literal("neighbors_update")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(DebugRenderersCommand::neighbors)))
                .then(Commands.literal("goals_selector")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(DebugRenderersCommand::goals)))

                ;
    }

    private static int navigation(CommandContext<CommandSourceStack> context) {
        debugNavigation = BoolArgumentType.getBool(context, "active");
        context.getSource().sendSuccess(() ->
                Component.literal("Pathfinding Debug Renderers " + debugNavigation), false);
        return 0;
    }

    private static int neighbors(CommandContext<CommandSourceStack> context) {
        debugNeighbors = BoolArgumentType.getBool(context, "active");
        context.getSource().sendSuccess(() ->
                Component.literal("Neighbor Updates Debug Renderers " + debugNeighbors), false);
        return 0;
    }


    private static int goals(CommandContext<CommandSourceStack> context) {
        debugGoals = BoolArgumentType.getBool(context, "active");
        context.getSource().sendSuccess(() ->
                Component.literal("Goals Selector Debug Renderers " + debugNeighbors), false);
        return 0;
    }


    public static boolean debugNavigation = false;
    public static boolean debugNeighbors = false;
    public static boolean debugGoals = false;
}