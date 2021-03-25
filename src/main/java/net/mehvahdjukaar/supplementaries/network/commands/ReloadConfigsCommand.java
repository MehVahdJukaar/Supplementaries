package net.mehvahdjukaar.supplementaries.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class ReloadConfigsCommand implements Command<CommandSource> {

    private static final ReloadConfigsCommand CMD = new ReloadConfigsCommand();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("reload")
                .requires((p) -> p.hasPermission(0))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        //TODO: figure out server/client side
        ClientConfigs.cached.refresh();
        ConfigHandler.syncServerConfigs();
        ServerConfigs.cached.refresh();
        context.getSource().sendSuccess(new TranslationTextComponent("message.supplementaries.command.configs_reloaded"), false);
        return 0;
    }
}