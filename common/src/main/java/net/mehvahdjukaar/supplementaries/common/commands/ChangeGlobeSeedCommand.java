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

import java.util.Random;

public class ChangeGlobeSeedCommand implements Command<CommandSourceStack> {

    private static final Random rand = new Random();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext dispatcher) {
        return Commands.literal("newseed")
                .requires(cs -> cs.hasPermission(2))
                .executes(new ChangeGlobeSeedCommand());
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = context.getSource().getLevel();
        GlobeData.recreateFromSeed(level, rand.nextLong());
        GlobeData.get(level).sendToClient(level);
        context.getSource().sendSuccess(() -> Component.translatable("message.supplementaries.command.globe_changed"), false);
        return 0;
    }
}