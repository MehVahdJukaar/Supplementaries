package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadConfigsCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("reload")
                .requires((p) -> p.hasPermission(0))
                .executes(new ReloadConfigsCommand());
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        //TODO: figure out server/client side
        CommonConfigs.SPEC.sendSyncedConfigsToAllPlayers();
        context.getSource().sendSuccess(()->Component.translatable("message.supplementaries.command.configs_reloaded"), false);
        return 0;
    }
}