package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class RecordSongCommand {

    @ExpectPlatform
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext dispatcher) {
        throw new AssertionError();
    }

}