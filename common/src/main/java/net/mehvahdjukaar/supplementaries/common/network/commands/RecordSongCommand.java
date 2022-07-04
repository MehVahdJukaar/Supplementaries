package net.mehvahdjukaar.supplementaries.common.network.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.server.command.EnumArgument;

public class RecordSongCommand {


    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("record")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("stop").executes(c -> RecordSongCommand.stop(c, "", 0))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> RecordSongCommand.stop(c, StringArgumentType.getString(c, "name"), 0))
                                .then(Commands.argument("speed_up_by", IntegerArgumentType.integer())
                                        .executes(c -> RecordSongCommand.stop(c,
                                                StringArgumentType.getString(c, "name"),
                                                IntegerArgumentType.getInteger(c, "speed_up_by")))
                                )))
                .then(Commands.literal("start").executes(RecordSongCommand::start)
                        .then(Commands.argument("instrument_0",
                                        EnumArgument.enumArgument(NoteBlockInstrument.class))
                                .executes(c -> RecordSongCommand.start(c, c.getArgument("instrument_0", NoteBlockInstrument.class)))
                                .then(Commands.argument("instrument_1",
                                                EnumArgument.enumArgument(NoteBlockInstrument.class))
                                        .executes(c -> RecordSongCommand.start(c,
                                                c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                c.getArgument("instrument_1", NoteBlockInstrument.class)))
                                        .then(Commands.argument("instrument_2",
                                                        EnumArgument.enumArgument(NoteBlockInstrument.class))
                                                .executes(c -> RecordSongCommand.start(c,
                                                        c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                        c.getArgument("instrument_1", NoteBlockInstrument.class),
                                                        c.getArgument("instrument_2", NoteBlockInstrument.class)))
                                                .then(Commands.argument("instrument_3",
                                                                EnumArgument.enumArgument(NoteBlockInstrument.class))
                                                        .executes(c -> RecordSongCommand.start(c,
                                                                c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                                c.getArgument("instrument_1", NoteBlockInstrument.class),
                                                                c.getArgument("instrument_2", NoteBlockInstrument.class),
                                                                c.getArgument("instrument_3", NoteBlockInstrument.class)))
                                                )))));
    }

    public static int stop(CommandContext<CommandSourceStack> context, String name, int speedup) throws CommandSyntaxException {

        String savedName = SongsManager.stopRecording(context.getSource().getLevel(), name, speedup);

        context.getSource().sendSuccess(Component.translatable("message.supplementaries.command.record.stop", savedName), false);

        return 0;
    }

    public static int start(CommandContext<CommandSourceStack> context, NoteBlockInstrument... whitelist) throws CommandSyntaxException {

        SongsManager.startRecording(whitelist);

        context.getSource().sendSuccess(Component.translatable("message.supplementaries.command.record.start"), false);

        return 0;
    }
}