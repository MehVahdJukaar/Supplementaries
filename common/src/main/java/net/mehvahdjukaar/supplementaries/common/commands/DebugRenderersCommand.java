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

public class DebugRenderersCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("debug")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("navigation")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(new DebugRenderersCommand())));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        debugNavigation = BoolArgumentType.getBool(context, "active");
        context.getSource().sendSuccess(() ->
                Component.literal("Pathfinding Debug Renderers " + debugNavigation), false);

        return 0;
    }


    public static boolean debugNavigation = false;
}