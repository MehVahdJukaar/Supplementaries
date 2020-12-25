package net.mehvahdjukaar.supplementaries.network.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> mymod = dispatcher.register(
                Commands.literal(Supplementaries.MOD_ID)
                        .requires((p) -> p.hasPermissionLevel(2))
                        .then(Commands.literal("globe")
                                .then(ChangeGlobeSeedCommand.register(dispatcher))
                                .then(ResetGlobeSeedCommand.register(dispatcher))

                        )
        );

        dispatcher.register(Commands.literal("splm").redirect(mymod));
    }
}
