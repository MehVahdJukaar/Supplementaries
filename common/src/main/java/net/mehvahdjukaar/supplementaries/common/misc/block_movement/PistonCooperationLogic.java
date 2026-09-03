package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

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

        long tick = level.getGameTime();
        PistonCooperationData data = PistonCooperationData.get(serverLevel);
        data.markAttempting(pos, facing, true, tick);

        if (resolveAlone.getAsBoolean()) {
            data.markPosted(pos, tick);
            return true;
        }

        Set<BlockPos> cooperators = PistonCooperationData.getCooperators(level, pos, facing, true, tick);
        if (cooperators.isEmpty()) return false;
        if (!resolveAsOneStructure(resolver, cooperators, pos, facing, true)) return false;

        for (BlockPos cooperatorPos : getContributingCooperators(resolver)) {
            if (data.hasPosted(cooperatorPos, tick)) continue;
            level.blockEvent(cooperatorPos, level.getBlockState(cooperatorPos).getBlock(), 0, facing.get3DDataValue());
            data.markPosted(cooperatorPos, tick);
        }
        data.markPosted(pos, tick);
        return true;
    }

    public static void markRetractingThisTick(Level level, BlockPos pos, Direction facing) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        PistonCooperationData.get(serverLevel).markAttempting(pos, facing, false, level.getGameTime());
    }

    //extension was already decided in checkIfExtend, here we just re apply the cooperators
    public static boolean resolveMoveTogether(PistonStructureResolver resolver, BooleanSupplier resolveAlone,
                                              Level level, BlockPos pos, Direction facing, boolean extending) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return resolveAlone.getAsBoolean();

        Set<BlockPos> cooperators = PistonCooperationData.getCooperators(level, pos, facing, extending, level.getGameTime());
        if (cooperators.isEmpty()) return resolveAlone.getAsBoolean();

        if (!extending) {
            if (resolveAlone.getAsBoolean()) return true;
            for (BlockPos cooperatorPos : cooperators) {
                retractCooperatorEarly(level, cooperatorPos, facing);
            }
        }
        return resolveAsOneStructure(resolver, cooperators, pos, facing, extending);
    }

    private static boolean resolveAsOneStructure(PistonStructureResolver resolver, Set<BlockPos> cooperators,
                                                 BlockPos pos, Direction facing, boolean extending) {
        Set<BlockPos> allPistons = new HashSet<>(cooperators);
        allPistons.add(pos);
        ((ICooperativePiston) resolver).supp$setCooperators(allPistons, facing, extending);
        return resolver.resolve();
    }

    private static Iterable<BlockPos> getContributingCooperators(PistonStructureResolver resolver) {
        return ((ICooperativePiston) resolver).supp$getCooperationState().getContributingCooperators();
    }

    private static void retractCooperatorEarly(Level level, BlockPos cooperatorPos, Direction facing) {
        BlockState bodyState = level.getBlockState(cooperatorPos);
        if (!(bodyState.getBlock() instanceof PistonBaseBlock pb)) return;

        BlockPos headPos = cooperatorPos.relative(facing);
        if (!level.getBlockState(headPos).is(Blocks.PISTON_HEAD)) return;

        PistonType type = pb.isSticky ? PistonType.STICKY : PistonType.DEFAULT;

        BlockState movingPistonState = Blocks.MOVING_PISTON.defaultBlockState()
                .setValue(MovingPistonBlock.FACING, facing)
                .setValue(MovingPistonBlock.TYPE, type);
        BlockState retractedBody = bodyState.setValue(PistonBaseBlock.EXTENDED, false);
        level.setBlock(cooperatorPos, movingPistonState, 20);
        level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(
                cooperatorPos, movingPistonState, retractedBody, facing, false, true));

        level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 20);
    }
}
