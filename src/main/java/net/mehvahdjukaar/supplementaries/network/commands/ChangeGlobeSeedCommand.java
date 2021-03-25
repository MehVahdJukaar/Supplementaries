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

import java.util.Random;

public class ChangeGlobeSeedCommand implements Command<CommandSource> {

    private static final ChangeGlobeSeedCommand CMD = new ChangeGlobeSeedCommand();
    private static final Random rand = new Random();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("newseed")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        GlobeData data = GlobeData.get(context.getSource().getLevel());
        data.seed=rand.nextLong();
        data.updateData();
        data.syncData(context.getSource().getLevel());
        context.getSource().sendSuccess(new TranslationTextComponent("message.supplementaries.command.globe_changed"), false);
        return 0;
    }
}