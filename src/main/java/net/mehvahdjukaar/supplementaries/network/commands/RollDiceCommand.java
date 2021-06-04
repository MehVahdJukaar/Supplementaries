package net.mehvahdjukaar.supplementaries.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Random;

public class RollDiceCommand implements Command<CommandSource> {

    private static final RollDiceCommand CMD = new RollDiceCommand();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("roll")
                .requires(cs -> cs.hasPermission(0))
                .then(Commands.argument("dice", IntegerArgumentType.integer(1))
                    .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Random r = new Random();
        int dice = IntegerArgumentType.getInteger(context, "dice");

        int roll = r.nextInt(dice);
        context.getSource().sendSuccess(new TranslationTextComponent("message.supplementaries.command.dice",dice, roll), false);

        return 0;
    }
}