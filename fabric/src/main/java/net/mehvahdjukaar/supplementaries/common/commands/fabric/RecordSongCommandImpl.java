package net.mehvahdjukaar.supplementaries.common.commands.fabric;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.mehvahdjukaar.supplementaries.common.commands.arguments.fabric.NoteBlockInstrumentArgument;
import net.mehvahdjukaar.supplementaries.common.commands.arguments.fabric.SourceArgument;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

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
                        .then(Commands.argument("source", new SourceArgument())
                                .executes(RecordSongCommandImpl::start)
                                .then(Commands.argument("instrument_0",
                                                new NoteBlockInstrumentArgument())
                                        .executes(c -> RecordSongCommandImpl.start(c, c.getArgument("instrument_0", NoteBlockInstrument.class)))
                                        .then(Commands.argument("instrument_1",
                                                        new NoteBlockInstrumentArgument())
                                                .executes(c -> RecordSongCommandImpl.start(c,
                                                        c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                        c.getArgument("instrument_1", NoteBlockInstrument.class)))
                                                .then(Commands.argument("instrument_2",
                                                                new NoteBlockInstrumentArgument())
                                                        .executes(c -> RecordSongCommandImpl.start(c,
                                                                c.getArgument("instrument_0", NoteBlockInstrument.class),
                                                                c.getArgument("instrument_1", NoteBlockInstrument.class),
                                                                c.getArgument("instrument_2", NoteBlockInstrument.class)))
                                                        .then(Commands.argument("instrument_3",
                                                                        new NoteBlockInstrumentArgument())
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
