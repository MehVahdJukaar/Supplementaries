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

// One unit of motion for a pulley pull, mirroring vanilla PistonBaseBlock.moveBlocks: each call
// shifts every block in the resolver's toPush list one slot toward the cooperating pulleys.
// This is the continuous-mode path (pulley_block.continuous_retraction = true), where the whole
// hanging structure moves over several ticks as moving block entities. RopeMover is the instant
// single-block alternative used by rope items, rope arrows and legacy-mode pulleys.
// Animation runs through MovingPulleyBlock, a subclass of vanilla MOVING_PISTON, so every
// sticky-block-aware mod that plugs into piston pushing extends to pulley pulling for free.
// The source-piston flag is never set. Vanilla marks one moving block as source so its final state
// becomes AIR (the head retracts away); we need no such cleanup, since the topmost rope is destroyed
// inline by moveOneStep and its old slot ends up unclaimed in vacatedSlots and cleared to air, which
// is exactly the rope consumption we want.
public final class PulleyMover {

    // Resolves and executes one unit pull step. animationTicks of 0 or 1 falls back to the vanilla
    // 2-tick speed. False means the resolve failed or there was nothing to pull, and the caller
    // should stop pulling; true means the blocks are now moving entities settling into place.
    public static boolean moveOneStep(Level level, PulleyStructureResolver resolver, int animationTicks) {
        if (!resolver.resolve()) {
            return false;
        }

        List<BlockPos> toPush = resolver.getToPush();
        if (toPush.isEmpty() && resolver.getToDestroy().isEmpty() && resolver.getDirectRopePlacements().isEmpty()) {
            return false;
        }

        Direction pushDir = resolver.getPushDirection();

        // Source position -> the state that was there. Entries are removed as they get claimed as
        // some other block's destination, so whatever is left over is a slot nothing moves into.
        Map<BlockPos, BlockState> vacatedSlots = new HashMap<>();
        List<BlockState> originalStates = new ArrayList<>();
        // Capture BE NBT for source positions that carry one. Keyed by srcPos so the loop below can
        // find it by position lookup. Values are sanitized via saveWithoutMetadata to drop the
        // id/x/y/z that would otherwise conflict with the destination position.
        Map<BlockPos, CompoundTag> carriedBeNbt = new HashMap<>();
        for (BlockPos pos : toPush) {
            BlockState state = level.getBlockState(pos);
            originalStates.add(state);
            vacatedSlots.put(pos, state);
            if (state.hasBlockEntity()) {
                CompoundTag nbt = PistonMovementHelper.captureAndDetachBlockEntity(level, pos);
                if (nbt != null) carriedBeNbt.put(pos.immutable(), nbt);
            }
        }
        // Snapshot consumed-rope states before the upcoming setBlock loop overwrites them with
        // MOVING_PULLEY. We need these intact so the topmost piston can render the consumed
        // rope sliding into the pulley as a "leading phantom" (no actual placement at the end).
        Map<BlockPos, BlockState> consumedRopeStates = new HashMap<>();
        for (BlockPos consumedPos : resolver.getConsumedRopes()) {
            consumedRopeStates.put(consumedPos.immutable(), level.getBlockState(consumedPos));
        }
        // For extend: the topmost piston (whose srcPos == firstSlot) carries a "trailing"
        // phantom rope that animates out of the pulley body and settles into firstSlot when
        // the animation ends. Empty in retract mode.
        Map<BlockPos, BlockState> extendingPhantomStates = resolver.getExtendingPhantomSources();

        List<BlockPos> destroyList = resolver.getToDestroy();

        for (int j = destroyList.size() - 1; j >= 0; --j) {
            BlockPos pos = destroyList.get(j);
            BlockState destroyState = level.getBlockState(pos);
            BlockEntity be = destroyState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(destroyState, level, pos, be);
            // Matches the call NeoForge-patched PistonBaseBlock.moveBlocks makes for each
            // toDestroy block, giving mods their IBlockExtension.onDestroyedByPushReaction hook
            // (no-op on Fabric, which has no equivalent extension).
            SuppPlatformStuff.onDestroyedByPushReaction(destroyState, level, pos, pushDir);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(destroyState));
        }

        // Always our own moving block: only it carries the extend phantom rope and the retract
        // leading-rope state. Vanilla MOVING_PISTON drops both, which made manual / wrench-driven
        // extends (animationTicks <= 1) shift the column down without ever placing the new rope.
        // At animationTicks <= 1 setAnimationDuration falls back to the vanilla 2-tick speed, so
        // timing is unchanged; we just keep the rope placement working.
        Block movingBlock = ModRegistry.MOVING_PULLEY_BLOCK.get();
        for (int j = toPush.size() - 1; j >= 0; --j) {
            BlockPos srcPos = toPush.get(j);
            BlockState srcState = originalStates.get(j);
            BlockPos dstPos = srcPos.relative(pushDir);
            vacatedSlots.remove(dstPos);
            BlockState movingState = movingBlock.defaultBlockState()
                    .setValue(MovingPistonBlock.FACING, pushDir)
                    .setValue(MovingPistonBlock.TYPE, PistonType.DEFAULT);
            level.setBlock(dstPos, movingState, 68);
            MovingPulleyBlockEntity movingBe = MovingPulleyBlock.newMovingBlockEntity(
                    dstPos, movingState, srcState, pushDir, true, false);
            movingBe.setAnimationDuration(animationTicks);
            // Retract: dstPos is the consumed firstSlot, so the phantom slides into the pulley.
            BlockState consumedHere = consumedRopeStates.get(dstPos);
            if (consumedHere != null) {
                movingBe.setLeadingState(consumedHere);
            }
            // Extend: srcPos is firstSlot, so the phantom emerges from the pulley body and lands
            // at firstSlot when the animation ends.
            BlockState extendPhantomHere = extendingPhantomStates.get(srcPos);
            if (extendPhantomHere != null) {
                movingBe.setLeadingState(extendPhantomHere);
                movingBe.setExtendPhantom(true);
            }
            CompoundTag srcBeNbt = carriedBeNbt.get(srcPos);
            if (srcBeNbt != null) {
                ((ICarryingMovingPiston) movingBe).supp$setCarriedBlockEntityNbt(srcBeNbt);
            }
            level.setBlockEntity(movingBe);
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos vacated : vacatedSlots.keySet()) {
            level.setBlock(vacated, air, 82);
        }

        // Extend-into-open-air: pulleys with only air below have no moved block to carry a phantom
        // rope, so drop the fresh rope straight into firstSlot. Guard on air so we never clobber a
        // block that arrived meanwhile. The spool decrement + place sound happen in
        // PulleyBlock.serverFinaliseExtend (these pulleys are marked as contributing).
        for (Map.Entry<BlockPos, BlockState> entry : resolver.getDirectRopePlacements().entrySet()) {
            if (level.getBlockState(entry.getKey()).isAir()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
        }

        for (Map.Entry<BlockPos, BlockState> entry : vacatedSlots.entrySet()) {
            BlockPos pos = entry.getKey();
            entry.getValue().updateIndirectNeighbourShapes(level, pos, 2);
            air.updateNeighbourShapes(level, pos, 2);
            air.updateIndirectNeighbourShapes(level, pos, 2);
        }

        for (int k = destroyList.size() - 1; k >= 0; --k) {
            BlockPos pos = destroyList.get(k);
            level.getBlockState(pos).updateIndirectNeighbourShapes(level, pos, 2);
            level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        }

        for (int k = toPush.size() - 1; k >= 0; --k) {
            BlockPos pos = toPush.get(k);
            level.updateNeighborsAt(pos, originalStates.get(k).getBlock());
        }

        return true;
    }
}
