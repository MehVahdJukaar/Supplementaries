package net.mehvahdjukaar.supplementaries.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class ResetGlobeSeedCommand implements Command<CommandSource> {

    private static final ResetGlobeSeedCommand CMD = new ResetGlobeSeedCommand();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("resetseed")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getLevel();
        GlobeData data = GlobeData.get(world);
        data.seed = world.getSeed();
        data.updateData();
        data.syncData(world);
        context.getSource().sendSuccess(new TranslationTextComponent("message.supplementaries.command.globe_reset"), false);
        return 0;
    }
}