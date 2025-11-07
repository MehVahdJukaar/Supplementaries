package net.mehvahdjukaar.supplementaries.reg;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.commands.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void init() {
        RegHelper.addCommandRegistration(ModCommands::register);
        registerArguments();
    }

    @ExpectPlatform
    public static void registerArguments() {
        throw new AssertionError();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {

        var node = dispatcher.register(
                Commands.literal(Supplementaries.MOD_ID)
                        .then(Commands.literal("globe")
                                .requires((p) -> p.hasPermission(2))
                                .then(ChangeGlobeSeedCommand.register(context))
                                .then(ResetGlobeSeedCommand.register(context))
                        )
                        .then(Commands.literal("configs")
                                .then(OpenConfiguredCommand.register(context))
                                .then(ReloadConfigsCommand.register(context))
                        )
                        .then(AddCageMobCommand.register(context))
                        .then(RecordSongCommand.register(context))
                        .then(StructureMapCommand.register(context))
        );

        dispatcher.register(Commands.literal("supp").redirect(node));
    }
}
