package net.mehvahdjukaar.supplementaries.common.commands.arguments.fabric;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.mehvahdjukaar.moonlight.api.resources.recipe.fabric.ResourceConditionsBridge;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoteBlockInstrumentArgument implements ArgumentType<NoteBlockInstrument> {

    private static final DynamicCommandExceptionType INVALID_SONG = new DynamicCommandExceptionType(
            found -> Component.translatable("message.supplementaries.argument.instrument.invalid", SongsManager.Source.values(), found));

    @Override
    public NoteBlockInstrument parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        try {
            return NoteBlockInstrument.valueOf(string);
        } catch (IllegalArgumentException e) {
            throw INVALID_SONG.createWithContext(reader, string);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Stream.of(NoteBlockInstrument.values()).map(Enum::name), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Stream.of(NoteBlockInstrument.values()).map(Enum::name).collect(Collectors.toList());
    }
}
