package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class RecordSongCommand {

    @PlatformImpl
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext dispatcher) {
        throw new AssertionError();
    }
}