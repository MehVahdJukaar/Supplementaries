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

// continuous pulley motion, like PistonBaseBlock.moveBlocks. shifts the resolved structure one
// slot per call. RopeMover is the instant version
public final class PulleyMover {

    //resolver must have been resolved already, this just applies it
    public static void moveOneStep(Level level, PulleyStructureResolver resolver, int animationTicks) {
        List<BlockPos> toPush = resolver.getToPush();
        Direction pushDir = resolver.getPushDirection();

        //whatever is left over at the end is cleared to air
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
        //before the loop overwrites them, needed to render the rope sliding into the pulley
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
            //neoforge only
            SuppPlatformStuff.onDestroyedByPushReaction(destroyState, level, pos, pushDir);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(destroyState));
        }

        //always ours, even at vanilla speed. MOVING_PISTON drops the phantom rope states
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
            //retract, the phantom slides into the pulley
            BlockState consumedHere = consumedRopeStates.get(dstPos);
            if (consumedHere != null) {
                movingBe.setLeadingState(consumedHere);
            }
            //extend, the phantom comes out of the pulley and lands there
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

        //nothing moved to carry a phantom, place the rope directly
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
    }
}
