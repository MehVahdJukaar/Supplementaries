package net.mehvahdjukaar.supplementaries.common.commands.neoforge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.server.command.EnumArgument;

public class RecordSongCommandImpl {


    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext dispatcher) {
        return Commands.literal("record")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("stop").executes(c -> RecordSongCommandImpl.stop(c, "", 0))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> RecordSongCommandImpl.stop(c, StringArgumentType.getString(c, "name"), 0))
                                .then(Commands.argument("speed_up_by", IntegerArgumentType.integer())
                                        .executes(c -> RecordSongCommandImpl.stop(c,
                                                StringArgumentType.getString(c, "name"),
                                                IntegerArgumentType.getInteger(c, "speed_up_by")))
                                )))
                .then(Commands.literal("start")
                        .then(Commands.argument("source", EnumArgument.enumArgument(SongsManager.Source.class))
                                .executes(RecordSongCommandImpl::start)
                                .then(Commands.argument("instrument_0",
                                                EnumArgument.enumArgument(NoteBlockInstrument.class))
                                        .executes(c -> RecordSongCommandImpl.start(c, c.getArgument("instrument_0", NoteBlockInstrument.class)))
                                        .then(Commands.argument("instrument_1",
                                                        EnumArgument.enumArgument(NoteBlockInstrument.class))
                                                .executes(c -> RecordSongCommandImpl.start(c,
                                                        c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                        c.getArgument("instrument_1", NoteBlockInstrument.class)))
                                                .then(Commands.argument("instrument_2",
                                                                EnumArgument.enumArgument(NoteBlockInstrument.class))
                                                        .executes(c -> RecordSongCommandImpl.start(c,
                                                                c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                                c.getArgument("instrument_1", NoteBlockInstrument.class),
                                                                c.getArgument("instrument_2", NoteBlockInstrument.class)))
                                                        .then(Commands.argument("instrument_3",
                                                                        EnumArgument.enumArgument(NoteBlockInstrument.class))
                                                                .executes(c -> RecordSongCommandImpl.start(c,
                                                                        c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                                        c.getArgument("instrument_1", NoteBlockInstrument.class),
                                                                        c.getArgument("instrument_2", NoteBlockInstrument.class),
                                                                        c.getArgument("instrument_3", NoteBlockInstrument.class)))
                                                        ))))));
    }

    public static int stop(CommandContext<CommandSourceStack> context, String name, int speedup) throws CommandSyntaxException {

        String savedName = SongsManager.stopRecording(context.getSource().getLevel(), name, speedup);

        context.getSource().sendSuccess(() -> Component.translatable("message.supplementaries.command.record.stop", savedName), false);

        return 0;
    }

    public static int start(CommandContext<CommandSourceStack> context, NoteBlockInstrument... whitelist) throws CommandSyntaxException {

        SongsManager.Source source = context.getArgument("source", SongsManager.Source.class);
        SongsManager.startRecording(source, whitelist);

        context.getSource().sendSuccess(() -> Component.translatable("message.supplementaries.command.record.start"), false);

        return 0;
    }


}