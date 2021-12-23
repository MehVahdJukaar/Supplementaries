package net.mehvahdjukaar.supplementaries.common.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Random;

public class IUsedToRollTheDice implements Command<CommandSourceStack> {

    private static final IUsedToRollTheDice CMD = new IUsedToRollTheDice();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("roll")
                .requires(cs -> cs.hasPermission(0))
                .then(Commands.argument("dice", IntegerArgumentType.integer(1))
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Random r = new Random();
        int dice = IntegerArgumentType.getInteger(context, "dice");

        int roll = r.nextInt(dice);
        context.getSource().sendSuccess(new TranslatableComponent("message.supplementaries.command.dice", dice, roll), false);

        return 0;
    }
}