package net.mehvahdjukaar.supplementaries.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.ResourceLocation;

public class AddCageMobCommand implements Command<CommandSource> {

    private static final AddCageMobCommand CMD = new AddCageMobCommand();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("cage")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("entity", EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {

        ResourceLocation id = EntitySummonArgument.getSummonableEntity( context,"entity");

        CapturedMobsHelper.COMMAND_MOBS.add(id.toString());
        return 0;
    }
}