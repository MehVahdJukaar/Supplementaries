package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MovingPulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingPulleyBlockEntity;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// continuous pulley motion, like PistonBaseBlock.moveBlocks.
// InstantPulleyMover is the instant version
public final class ContinuousPulleyMover {

    public static void moveOneStep(Level level, PulleyStructureResolver resolvedStructure, int animationTicks) {
        List<BlockPos> toPush = resolvedStructure.getToPush();
        Direction pushDir = resolvedStructure.getPushDirection();

        Map<BlockPos, BlockState> slotsNotRefilledByMove = new HashMap<>();
        List<BlockState> originalStates = new ArrayList<>();
        Map<BlockPos, CompoundTag> carriedBeNbt = new HashMap<>();
        for (BlockPos pos : toPush) {
            BlockState state = level.getBlockState(pos);
            originalStates.add(state);
            slotsNotRefilledByMove.put(pos, state);
            if (state.hasBlockEntity()) {
                CompoundTag nbt = BlockMovementHelper.captureAndDetachBlockEntity(level, pos);
                if (nbt != null) carriedBeNbt.put(pos.immutable(), nbt);
            }
        }
        Map<BlockPos, BlockState> retractedRopeStatesBeforeOverwrite = new HashMap<>();
        for (BlockPos retractedPos : resolvedStructure.getRopesRetractedIntoPulleys()) {
            retractedRopeStatesBeforeOverwrite.put(retractedPos.immutable(), level.getBlockState(retractedPos));
        }
        Map<BlockPos, BlockState> ropesEmergingFromPulleys = resolvedStructure.getRopesEmergingFromPulleys();

        List<BlockPos> toDestroy = resolvedStructure.getToDestroy();
        destroyBlocksInTheWay(level, toDestroy, pushDir);

        Block movingBlock = ModRegistry.MOVING_PULLEY_BLOCK.get();
        for (int j = toPush.size() - 1; j >= 0; --j) {
            BlockPos srcPos = toPush.get(j);
            BlockState srcState = originalStates.get(j);
            BlockPos dstPos = srcPos.relative(pushDir);
            slotsNotRefilledByMove.remove(dstPos);
            BlockState movingState = movingBlock.defaultBlockState()
                    .setValue(MovingPistonBlock.FACING, pushDir)
                    .setValue(MovingPistonBlock.TYPE, PistonType.DEFAULT);
            level.setBlock(dstPos, movingState, 68);
            MovingPulleyBlockEntity movingBe = MovingPulleyBlock.newMovingBlockEntity(
                    dstPos, movingState, srcState, pushDir, true, false);
            movingBe.setAnimationDuration(animationTicks);
            BlockState ropeSlidingIntoPulley = retractedRopeStatesBeforeOverwrite.get(dstPos);
            if (ropeSlidingIntoPulley != null) {
                movingBe.setLeadingState(ropeSlidingIntoPulley);
            }
            BlockState ropeEmergingFromPulley = ropesEmergingFromPulleys.get(srcPos);
            if (ropeEmergingFromPulley != null) {
                movingBe.setLeadingState(ropeEmergingFromPulley);
                movingBe.setExtendPhantom(true);
            }
            CompoundTag srcBeNbt = carriedBeNbt.get(srcPos);
            if (srcBeNbt != null) {
                ((ICarryingMovingPiston) movingBe).supp$setCarriedBlockEntityNbt(srcBeNbt);
            }
            level.setBlockEntity(movingBe);
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos vacated : slotsNotRefilledByMove.keySet()) {
            level.setBlock(vacated, air, 82);
        }

        for (Map.Entry<BlockPos, BlockState> entry : resolvedStructure.getRopesPlacedWithoutAnimation().entrySet()) {
            if (level.getBlockState(entry.getKey()).isAir()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
        }

        updateNeighboursLikeVanillaPiston(level, slotsNotRefilledByMove, toDestroy, toPush, originalStates);
    }

    private static void destroyBlocksInTheWay(Level level, List<BlockPos> toDestroy, Direction pushDir) {
        for (int j = toDestroy.size() - 1; j >= 0; --j) {
            BlockPos pos = toDestroy.get(j);
            BlockState destroyState = level.getBlockState(pos);
            BlockEntity be = destroyState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(destroyState, level, pos, be);
            SuppPlatformStuff.onDestroyedByPushReaction(destroyState, level, pos, pushDir);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(destroyState));
        }
    }

    private static void updateNeighboursLikeVanillaPiston(Level level, Map<BlockPos, BlockState> slotsNotRefilledByMove,
                                                          List<BlockPos> toDestroy, List<BlockPos> toPush, List<BlockState> originalStates) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (Map.Entry<BlockPos, BlockState> entry : slotsNotRefilledByMove.entrySet()) {
            BlockPos pos = entry.getKey();
            entry.getValue().updateIndirectNeighbourShapes(level, pos, 2);
            air.updateNeighbourShapes(level, pos, 2);
            air.updateIndirectNeighbourShapes(level, pos, 2);
        }

        for (int k = toDestroy.size() - 1; k >= 0; --k) {
            BlockPos pos = toDestroy.get(k);
            level.getBlockState(pos).updateIndirectNeighbourShapes(level, pos, 2);
            level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        }

        for (int k = toPush.size() - 1; k >= 0; --k) {
            BlockPos pos = toPush.get(k);
            level.updateNeighborsAt(pos, originalStates.get(k).getBlock());
        }
    }
}
