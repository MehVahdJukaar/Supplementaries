package net.mehvahdjukaar.supplementaries.common.network.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
                Commands.literal(Supplementaries.MOD_ID)
                        .then(Commands.literal("globe")
                                .requires((p) -> p.hasPermission(2))
                                .then(ChangeGlobeSeedCommand.register(dispatcher))
                                .then(ResetGlobeSeedCommand.register(dispatcher))
                        )
                        .then(ReloadConfigsCommand.register(dispatcher))
                        .then(OpenConfiguredCommand.register(dispatcher))
                        .then(IUsedToRollTheDice.register(dispatcher))
                        .then(AddCageMobCommand.register(dispatcher))
                        .then(RecordSongCommand.register(dispatcher))
        );

        //dispatcher.register(Commands.literal("splm").redirect(commands));
    }
}
