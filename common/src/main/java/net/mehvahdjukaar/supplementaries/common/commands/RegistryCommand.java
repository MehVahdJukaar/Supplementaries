package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistryCommand {
    private static final long PAGE_SIZE = 8L;
    private static final ResourceKey<Registry<Registry<?>>> ROOT_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("root"));
    private static final DynamicCommandExceptionType UNKNOWN_REGISTRY = new DynamicCommandExceptionType((key) ->
            Component.translatable("commands.supplementaries.registry.error.unknown_registry", key.toString()));

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("registry")
                .requires((cs) -> cs.hasPermission(2))
                .then(Commands.argument("registry", ResourceKeyArgument.key(ROOT_REGISTRY_KEY))
                        .suggests(RegistryCommand::suggestRegistries)
                        .then(Commands.literal("list")
                                .executes((ctx) -> listElements(ctx, 1, null))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes((ctx) -> listElements(ctx, IntegerArgumentType.getInteger(ctx, "page"), null))
                                )
                        )
                        .then(Commands.literal("search")
                                .then(Commands.argument("keyword", StringArgumentType.string())
                                        .executes((ctx) -> listElements(ctx, 1, StringArgumentType.getString(ctx, "keyword")))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes((ctx) -> listElements(ctx, IntegerArgumentType.getInteger(ctx, "page"), StringArgumentType.getString(ctx, "keyword"))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("dump")
                                .requires(cs -> cs.hasPermission(3))
                                .then(Commands.argument("file_name", StringArgumentType.string())
                                        .executes((ctx) -> {
                                            String fileName = StringArgumentType.getString(ctx, "file_name");
                                            return dumpRegistry(ctx, fileName);
                                        })
                                )
                        )
                );
    }

    private static int dumpRegistry(CommandContext<CommandSourceStack> ctx, String fileName) throws CommandSyntaxException {
        ResourceKey<? extends Registry<?>> registryKey = getResourceKey(ctx, "registry", ROOT_REGISTRY_KEY).orElseThrow();
        CommandSourceStack source = ctx.getSource();
        Registry<?> registry = source.getServer().registryAccess().registry(registryKey).orElseThrow(() ->
                UNKNOWN_REGISTRY.create(registryKey.location()));

        var dir = PlatHelper.getGamePath().resolve("registry_dumps");

        if (!dir.toFile().exists()) {
            dir.toFile().mkdirs();
        }

        var file = dir.resolve(fileName + ".txt");
        try (var writer = new BufferedWriter(new FileWriter(file.toFile()))) {
            for (var entry : registry) {
                writer.write(entry.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new CommandSyntaxException(
                    new SimpleCommandExceptionType(() -> "I/O error"),
                    e::getMessage
            );
        }

        ctx.getSource().sendSuccess(() ->
                Component.translatable("commands.supplementaries.registry.dump.success",
                        file.toString()), false);

        return 0;
    }

    private static int listElements(CommandContext<CommandSourceStack> ctx, int page, @Nullable String search) throws CommandSyntaxException {
        ResourceKey<? extends Registry<?>> registryKey = getResourceKey(ctx, "registry", ROOT_REGISTRY_KEY).orElseThrow();
        CommandSourceStack source = ctx.getSource();
        Registry<?> registry = source.getServer().registryAccess().registry(registryKey).orElseThrow(() ->
                UNKNOWN_REGISTRY.create(registryKey.location()));
        long elementCount = registry.size();
        source.sendSuccess(() -> createMessage(
                        Component.translatable(
                                "commands.supplementaries.registry.registry_key",
                                Component.literal(registryKey.location().toString()).withStyle(ChatFormatting.GOLD)
                        ),
                        page,
                        () -> registry.keySet().stream().map(ResourceLocation::toString),
                        search),
                false);
        return (int) elementCount;
    }

    private static MutableComponent createMessage(MutableComponent header, long currentPage,
                                                  Supplier<Stream<String>> names, @Nullable String search) {
        List<String> filtered = names.get().filter((s) -> search == null || s.contains(search)).toList();
        long count = filtered.size();
        String allElementNames = filtered.stream().sorted().collect(Collectors.joining("\n"));
        long totalPages = (count - 1L) / PAGE_SIZE + 1L;
        long actualPage = (long) Mth.clamp((float) currentPage, 1.0F, (float) totalPages);
        MutableComponent component = Component.translatable("commands.supplementaries.registry.elements_count", count);
        if (count > 0L) {
            component = ComponentUtils.wrapInSquareBrackets(component.withStyle((s) -> s
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, allElementNames))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("commands.supplementaries.registry.copy_elements_names")))));
            component = Component.translatable("commands.supplementaries.registry.page_info", component, actualPage, totalPages);
        }

        MutableComponent tagElements = Component.literal("").append(component);
        Stream<MutableComponent> stream = filtered.stream().sorted().skip(PAGE_SIZE * (actualPage - 1L))
                .limit(PAGE_SIZE).map(Component::literal)
                .map((t) -> t.withStyle(ChatFormatting.DARK_GREEN))
                .map((t) -> Component.literal("\n - ").append(t));
        Objects.requireNonNull(tagElements);
        stream.forEach(tagElements::append);
        return header.append("\n").append(tagElements);
    }

    private static CompletableFuture<Suggestions> suggestRegistries(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Stream<String> strings = ctx.getSource().registryAccess().registries().map(RegistryAccess.RegistryEntry::key)
                .map(ResourceKey::location).map(ResourceLocation::toString);
        Objects.requireNonNull(builder);
        strings.forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static <T> Optional<ResourceKey<T>> getResourceKey(CommandContext<CommandSourceStack> ctx, String name, ResourceKey<Registry<T>> registryKey) {
        ResourceKey<?> key = ctx.getArgument(name, ResourceKey.class);
        return key.cast(registryKey);
    }
}
