package net.mehvahdjukaar.supplementaries.common.commands.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RecordSongCommandImpl {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("record").executes(c -> {
            c.getSource().sendSuccess(Component.literal("Record command has not been implemented for fabric"), false);
            return 0;
        });
    }
}
