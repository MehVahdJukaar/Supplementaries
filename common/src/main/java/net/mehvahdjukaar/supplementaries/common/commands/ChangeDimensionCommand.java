package net.mehvahdjukaar.supplementaries.common.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ChangeDimensionCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("dimension")
                .requires((p) -> p.hasPermission(2))
                .then(Commands.argument("dimension", new DimensionArgument())
                        .executes(c -> teleportToPos(c,
                                List.of(c.getSource().getEntityOrException()),
                                DimensionArgument.getDimension(c, "dimension"),
                                WorldCoordinates.current()))
                        .then(Commands.argument("location", Vec3Argument.vec3())
                                .executes((c) -> teleportToPos(c,
                                        List.of(c.getSource().getEntityOrException()),
                                        DimensionArgument.getDimension(c, "dimension"),
                                        Vec3Argument.getCoordinates(c, "location")))
                        )
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes((c) -> teleportToPos(c,
                                        EntityArgument.getEntities(c, "targets"),
                                        DimensionArgument.getDimension(c, "dimension"),
                                        WorldCoordinates.current()))
                                .then(Commands.argument("location", Vec3Argument.vec3())
                                        .executes((c) -> teleportToPos(c,
                                                EntityArgument.getEntities(c, "targets"),
                                                DimensionArgument.getDimension(c, "dimension"),
                                                Vec3Argument.getCoordinates(c, "location"))
                                        )
                                )
                        )
                );
    }


    private static int teleportToPos(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets,
                                     Level level,
                                     Coordinates position) throws CommandSyntaxException {
        var source = context.getSource();

        Vec3 vec3 = position.getPosition(source);
        Set<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        if (position.isXRelative()) {
            set.add(RelativeMovement.X);
        }

        if (position.isYRelative()) {
            set.add(RelativeMovement.Y);
        }

        if (position.isZRelative()) {
            set.add(RelativeMovement.Z);
        }

        set.add(RelativeMovement.X_ROT);
        set.add(RelativeMovement.Y_ROT);


        for (Entity entity : targets) {
            performTeleport(source, entity, level, vec3.x, vec3.y, vec3.z, set);
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single", targets.iterator().next().getDisplayName(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple", targets.size(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
        }

        return targets.size();

    }

    private static void performTeleport(CommandSourceStack source, Entity entity, Level level, double x, double y, double z, Set<RelativeMovement> relativeList) throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.containing(x, y, z);
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw INVALID_POSITION.create();
        } else {
            float f = Mth.wrapDegrees(entity.getYRot());
            float g = Mth.wrapDegrees(entity.getXRot());
            if (entity.teleportTo((ServerLevel) level, x, y, z, relativeList, f, g)) {

                label23:
                {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity.isFallFlying()) {
                            break label23;
                        }
                    }

                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                    entity.setOnGround(true);
                }

                if (entity instanceof PathfinderMob pathfinderMob) {
                    pathfinderMob.getNavigation().stop();
                }

            }
        }
    }


    private static String formatDouble(double d) {
        return String.format(Locale.ROOT, "%f", d);
    }

    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));


}