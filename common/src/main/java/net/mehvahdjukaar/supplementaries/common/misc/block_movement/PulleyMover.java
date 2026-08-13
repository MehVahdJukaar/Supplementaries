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

// Continuous-mode pulley motion, mirroring vanilla PistonBaseBlock.moveBlocks: each call shifts the
// resolved structure one slot via MovingPulleyBlock entities (a MOVING_PISTON subclass, so
// piston-aware mods work on pulleys too). RopeMover is the instant single-block alternative.
// No moving block is flagged as source: the consumed rope's slot is simply left unclaimed in
// vacatedSlots and cleared to air, which is exactly the rope consumption we want.
public final class PulleyMover {

    // False means the resolve failed or nothing moved, and the caller should stop pulling.
    public static boolean moveOneStep(Level level, PulleyStructureResolver resolver, int animationTicks) {
        if (!resolver.resolve()) {
            return false;
        }

        List<BlockPos> toPush = resolver.getToPush();
        if (toPush.isEmpty() && resolver.getToDestroy().isEmpty() && resolver.getDirectRopePlacements().isEmpty()) {
            return false;
        }

        Direction pushDir = resolver.getPushDirection();

        // Entries are removed as they get claimed as destinations; whatever is left over is a slot
        // nothing moves into, cleared to air.
        Map<BlockPos, BlockState> vacatedSlots = new HashMap<>();
        List<BlockState> originalStates = new ArrayList<>();
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
        // Snapshot before the setBlock loop overwrites these with MOVING_PULLEY, so the topmost
        // mover can render the consumed rope sliding into the pulley as a leading phantom.
        Map<BlockPos, BlockState> consumedRopeStates = new HashMap<>();
        for (BlockPos consumedPos : resolver.getConsumedRopes()) {
            consumedRopeStates.put(consumedPos.immutable(), level.getBlockState(consumedPos));
        }
        Map<BlockPos, BlockState> extendingPhantomStates = resolver.getExtendingPhantomSources();

        List<BlockPos> destroyList = resolver.getToDestroy();

        for (int j = destroyList.size() - 1; j >= 0; --j) {
            BlockPos pos = destroyList.get(j);
            BlockState destroyState = level.getBlockState(pos);
            BlockEntity be = destroyState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(destroyState, level, pos, be);
            // NeoForge's patched moveBlocks makes this call per destroyed block; no-op on Fabric.
            SuppPlatformStuff.onDestroyedByPushReaction(destroyState, level, pos, pushDir);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(destroyState));
        }

        // Always our own moving block, even at vanilla speed (animationTicks <= 1): vanilla
        // MOVING_PISTON drops the phantom rope states, so manual extends never placed the new rope.
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
            // Retract: dstPos is the consumed firstSlot; the phantom slides into the pulley.
            BlockState consumedHere = consumedRopeStates.get(dstPos);
            if (consumedHere != null) {
                movingBe.setLeadingState(consumedHere);
            }
            // Extend: srcPos is firstSlot; the phantom emerges from the pulley and lands there.
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

        // Extend-into-open-air: no moved block to carry a phantom, so place the rope directly.
        // The air guard avoids clobbering a block that arrived meanwhile.
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
