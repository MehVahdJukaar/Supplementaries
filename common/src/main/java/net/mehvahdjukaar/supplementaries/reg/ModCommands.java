package net.mehvahdjukaar.supplementaries.reg;

import com.mojang.brigadier.CommandDispatcher;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.commands.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void init() {
        RegHelper.addCommandRegistration(ModCommands::register);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(
                Commands.literal(Supplementaries.MOD_ID)
                        .then(Commands.literal("globe")
                                .requires((p) -> p.hasPermission(2))
                                .then(ChangeGlobeSeedCommand.register(dispatcher))
                                .then(ResetGlobeSeedCommand.register(dispatcher))
                        )
                        .then(ReloadConfigsCommand.register(dispatcher))
                        .then(OpenConfiguredCommand.register(dispatcher))
                        .then(AddCageMobCommand.register(dispatcher, context))
                        .then(RecordSongCommand.register(dispatcher))
                        .then(Commands.literal("map")
                                .requires((p) -> p.hasPermission(2))
                                .then(MapMarkerCommand.register(dispatcher, context))
                                .then(StructureMapCommand.register(dispatcher, context))
                        )

        );
    }
}
