package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ResetGlobeSeedCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext dispatcher) {
        return Commands.literal("resetseed")
                .requires(cs -> cs.hasPermission(2))
                .executes(new ResetGlobeSeedCommand());
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = context.getSource().getLevel();
        GlobeData.changeDataWithSeed(level, level.getSeed());
        GlobeData.get(level).sendToClient(level);
        context.getSource().sendSuccess(() -> Component.translatable("message.supplementaries.command.globe_reset"), false);
        return 0;
    }
}