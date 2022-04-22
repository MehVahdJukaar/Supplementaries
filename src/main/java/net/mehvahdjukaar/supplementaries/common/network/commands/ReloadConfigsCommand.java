package net.mehvahdjukaar.supplementaries.common.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class ReloadConfigsCommand implements Command<CommandSourceStack> {

    private static final ReloadConfigsCommand CMD = new ReloadConfigsCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("reload")
                .requires((p) -> p.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        //TODO: figure out server/client side
        ClientConfigs.cached.refresh();
        ConfigHandler.sendSyncedConfigsToAllPlayers();
        ServerConfigs.cached.refresh();
        context.getSource().sendSuccess(new TranslatableComponent("message.supplementaries.command.configs_reloaded"), false);
        return 0;
    }
}