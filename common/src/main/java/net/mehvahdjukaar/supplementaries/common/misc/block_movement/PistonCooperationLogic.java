package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundPistonCooperatorsPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

//piston side of the cooperation stuff. the mixin just forwards here
public class PistonCooperationLogic {

    public static boolean tryExtendTogether(PistonStructureResolver resolver, BooleanSupplier resolveAlone,
                                            Level level, BlockPos pos, Direction facing) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return resolveAlone.getAsBoolean();
        if (!(level instanceof ServerLevel serverLevel)) return resolveAlone.getAsBoolean();

        PistonAttemptsTracker.mark(serverLevel, pos, facing, true);
        if (resolveAlone.getAsBoolean()) return true;

        Set<BlockPos> cooperators = PistonAttemptsTracker.getCooperators(serverLevel, pos, facing, true);
        if (cooperators.isEmpty()) return false;
        if (!resolveAsOneStructure(resolver, cooperators, pos, facing, true)) return false;

        for (BlockPos cooperatorPos : getContributingCooperators(resolver)) {
            level.blockEvent(cooperatorPos, level.getBlockState(cooperatorPos).getBlock(), 0, facing.get3DDataValue());
        }
        return true;
    }

    private static Set<BlockPos> getContributingCooperators(PistonStructureResolver resolver) {
        return ((ICooperativePistons) resolver).supp$getCooperationState().getContributingCooperators();
    }

    public static void markRetractingThisTick(Level level, BlockPos pos, Direction facing) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return;
        if (level instanceof ServerLevel serverLevel) {
            PistonAttemptsTracker.mark(serverLevel, pos, facing, false);
        }
    }

    public static boolean resolveMoveTogether(PistonStructureResolver resolver, BooleanSupplier resolveAlone,
                                              Level level, BlockPos pos, Direction facing, boolean extending) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return resolveAlone.getAsBoolean();

        Set<BlockPos> cooperators;
        if (level instanceof ServerLevel serverLevel) {
            if (resolveAlone.getAsBoolean()) return true;
            cooperators = PistonAttemptsTracker.getCooperators(serverLevel, pos, facing, extending);
            if (cooperators.isEmpty()) return false;
        } else {
            cooperators = PistonAttemptsTracker.takeClientCooperators(level, pos);
            if (cooperators == null) return resolveAlone.getAsBoolean();
        }

        boolean moved = resolveAsOneStructure(resolver, cooperators, pos, facing, extending);
        if (moved && !extending) {
            for (BlockPos cooperatorPos : getContributingCooperators(resolver))
                retractCooperator(level, cooperatorPos, facing);
        }
        if (moved && level instanceof ServerLevel serverLevel) {
            NetworkHelper.sendToAllClientPlayersInDefaultRange(serverLevel, pos,
                    new ClientBoundPistonCooperatorsPacket(pos, cooperators));
        }
        return moved;
    }

    private static boolean resolveAsOneStructure(PistonStructureResolver resolver, Set<BlockPos> cooperators,
                                                 BlockPos pos, Direction facing, boolean extending) {
        Set<BlockPos> allPistons = new HashSet<>(cooperators);
        allPistons.add(pos);
        ((ICooperativePistons) resolver).supp$setCooperators(allPistons, facing, extending);
        return resolver.resolve();
    }

    //same order as vanilla triggerEvent, body first so the head removal doesnt pop the base
    private static void retractCooperator(Level level, BlockPos cooperatorPos, Direction facing) {
        BlockState bodyState = level.getBlockState(cooperatorPos);
        if (!(bodyState.getBlock() instanceof PistonBaseBlock piston)) return;

        BlockState movingPistonState = Blocks.MOVING_PISTON.defaultBlockState()
                .setValue(MovingPistonBlock.FACING, facing)
                .setValue(MovingPistonBlock.TYPE, piston.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
        level.setBlock(cooperatorPos, movingPistonState, 20);
        level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(cooperatorPos, movingPistonState,
                bodyState.setValue(PistonBaseBlock.EXTENDED, false), facing, false, true));
        level.blockUpdated(cooperatorPos, Blocks.MOVING_PISTON);
        movingPistonState.updateNeighbourShapes(level, cooperatorPos, 2);

        BlockPos headPos = cooperatorPos.relative(facing);
        if (level.getBlockState(headPos).is(Blocks.PISTON_HEAD))
            level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 20);
    }
}
