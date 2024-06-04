package net.mehvahdjukaar.supplementaries.common.commands;


import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Objects;
import java.util.Optional;


public class StructureMapCommand {
    private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType((object) -> Component.translatable("commands.locate.structure.invalid", new Object[]{object}));

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("structure_map")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("structure", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE))
                        .executes(c -> StructureMapCommand.giveMap(c, 2))
                        .then(Commands.argument("zoom", IntegerArgumentType.integer())
                                .executes(c -> StructureMapCommand.giveMap(c, IntegerArgumentType.getInteger(c, "zoom")))
                        )
                );
    }


    public static int giveMap(CommandContext<CommandSourceStack> context, int zoom) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        var structure = ResourceOrTagKeyArgument.getResourceOrTagKey(context, "structure", Registries.STRUCTURE, ERROR_STRUCTURE_INVALID);
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        HolderSet<Structure> holderSet = getHolders(structure, registry).orElseThrow(() -> ERROR_STRUCTURE_INVALID.create(structure.asPrintable()));
        var p = source.getPlayer();
        if (p != null) {
            var item = AdventurerMapsHandler.createMapOrQuill(level, p.getOnPos(), holderSet,  150, true,zoom, null, null, 0);
            p.addItem(item);
        }
        return 0;
    }

    private static Optional<? extends HolderSet<Structure>> getHolders(
            ResourceOrTagKeyArgument.Result<Structure> structure,
            Registry<Structure> structureRegistry) {
        Objects.requireNonNull(structureRegistry);
        return structure.unwrap().map(res -> structureRegistry.getHolder(res).map(HolderSet::direct),
                structureRegistry::getTag);
    }
}