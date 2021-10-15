package net.mehvahdjukaar.supplementaries.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Random;

public class ChangeGlobeSeedCommand implements Command<CommandSourceStack> {

    private static final ChangeGlobeSeedCommand CMD = new ChangeGlobeSeedCommand();
    private static final Random rand = new Random();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("newseed")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        GlobeData data = GlobeData.get(context.getSource().getLevel());
        data.seed=rand.nextLong();
        data.updateData();
        data.syncData(context.getSource().getLevel());
        context.getSource().sendSuccess(new TranslatableComponent("message.supplementaries.command.globe_changed"), false);
        return 0;
    }
}