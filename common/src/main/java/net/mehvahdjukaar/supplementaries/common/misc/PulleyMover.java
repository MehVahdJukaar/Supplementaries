package net.mehvahdjukaar.supplementaries.common.misc;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MovingPulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingPulleyBlockEntity;
import net.mehvahdjukaar.supplementaries.common.utils.ICarryingMovingPiston;
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

/**
 * Executes a single unit-of-motion for a pulley pull, mirroring vanilla
 * {@code PistonBaseBlock.moveBlocks}. Each call shifts every block in the resolver's
 * {@code toPush} list by one slot toward the cooperating pulleys.
 * <p>
 * Animation reuses vanilla {@link Blocks#MOVING_PISTON} so every sticky-block-aware mod
 * that plugs into piston pushing automatically extends to pulley pulling as well.
 * <p>
 * <b>Source-piston flag is never set.</b> Vanilla pistons mark one moving block as
 * "source" so its final state becomes AIR (the piston head retracts away). For us, no
 * such cleanup is needed — the topmost rope is destroyed inline by
 * {@link #moveOneStep} via the moveBlocks {@code map} machinery (its old slot ends up in
 * {@code map} as a leftover, then gets cleared to air, which is the rope-consumption
 * effect we want).
 */
public final class PulleyMover {

    private PulleyMover() {
    }

    /**
     * Resolves and executes one unit pull step.
     *
     * @return true if the step succeeded (blocks have been turned into MOVING_PISTON
     * entities and will settle into their new positions over the next
     * {@code PistonMovingBlockEntity.TICKS_TO_EXTEND} ticks). False if the resolve
     * failed or there was nothing to pull — the caller should stop the pull.
     */
    /**
     * @param animationTicks animation duration. 0 = use vanilla {@link Blocks#MOVING_PISTON}
     *                       (2-tick animation). >0 = use {@code MovingPulleyBlock} (TODO once
     *                       subclass lands) with progress sized to last that many ticks.
     */
    public static boolean moveOneStep(Level level, PulleyStructureResolver resolver, int animationTicks) {
        if (!resolver.resolve()) {
            Supplementaries.LOGGER.info("[PulleyMover {}] resolve() returned false — aborting step",
                    level.isClientSide ? "client" : "server");
            return false;
        }

        List<BlockPos> list = resolver.getToPush();
        if (list.isEmpty() && resolver.getToDestroy().isEmpty()) {
            Supplementaries.LOGGER.info("[PulleyMover {}] toPush + toDestroy both empty — aborting step",
                    level.isClientSide ? "client" : "server");
            return false;
        }

        Direction pushDir = resolver.getPushDirection();
        Supplementaries.LOGGER.info("[PulleyMover {}] moving {} blocks (pushDir={}, dim={})",
                level.isClientSide ? "client" : "server",
                list.size(), pushDir, level.dimension().location());

        Map<BlockPos, BlockState> map = Maps.newHashMap();
        List<BlockState> originalStates = new ArrayList<>();
        // Capture BE NBT for source positions that carry one. Keyed by srcPos so the j-loop
        // below can find it by position lookup. Values are NBT (sanitized via saveWithoutMetadata
        // to drop the id/x/y/z that would otherwise conflict with the destination position).
        Map<BlockPos, CompoundTag> carriedBeNbt = new HashMap<>();
        for (BlockPos p : list) {
            BlockState s = level.getBlockState(p);
            originalStates.add(s);
            map.put(p, s);
            if (s.hasBlockEntity()) {
                BlockEntity be = level.getBlockEntity(p);
                if (be != null) {
                    carriedBeNbt.put(p.immutable(), be.saveWithoutMetadata(level.registryAccess()));
                    // Detach the BE from this position so the upcoming setBlock(AIR / MOVING_PISTON)
                    // doesn't drop it as items via vanilla onRemove.
                    level.removeBlockEntity(p);
                }
            }
        }

        List<BlockPos> destroyList = resolver.getToDestroy();

        for (int j = destroyList.size() - 1; j >= 0; --j) {
            BlockPos pos = destroyList.get(j);
            BlockState destroyState = level.getBlockState(pos);
            BlockEntity be = destroyState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(destroyState, level, pos, be);
            // Matches the call NeoForge-patched PistonBaseBlock.moveBlocks makes for each
            // toDestroy block — gives mods their {@code IBlockExtension.onDestroyedByPushReaction}
            // hook (no-op on Fabric, which has no equivalent extension).
            SuppPlatformStuff.onDestroyedByPushReaction(destroyState, level, pos, pushDir);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(destroyState));
        }

        // Pick the moving-block variant: our MovingPulleyBlock when a driver passed an animation
        // duration (we want a non-vanilla speed), else vanilla MOVING_PISTON for the no-driver
        // path (matches the player-shift-click case where vanilla 2-tick speed is fine).
        boolean usePulleyBlock = animationTicks > 1;
        Block movingBlock = usePulleyBlock ? ModRegistry.MOVING_PULLEY_BLOCK.get() : Blocks.MOVING_PISTON;
        for (int j = list.size() - 1; j >= 0; --j) {
            BlockPos srcPos = list.get(j);
            BlockState srcState = originalStates.get(j);
            BlockPos dstPos = srcPos.relative(pushDir);
            map.remove(dstPos);
            BlockState movingPistonState = movingBlock.defaultBlockState()
                    .setValue(MovingPistonBlock.FACING, pushDir)
                    .setValue(MovingPistonBlock.TYPE, PistonType.DEFAULT);
            level.setBlock(dstPos, movingPistonState, 68);
            BlockEntity movingBe = usePulleyBlock
                    ? MovingPulleyBlock.newMovingBlockEntity(dstPos, movingPistonState, srcState, pushDir, true, false)
                    : MovingPistonBlock.newMovingBlockEntity(dstPos, movingPistonState, srcState, pushDir, true, false);
            if (movingBe instanceof MovingPulleyBlockEntity mpbe) {
                mpbe.setAnimationDuration(animationTicks);
            }
            if (movingBe instanceof ICarryingMovingPiston carrier) {
                CompoundTag srcBeNbt = carriedBeNbt.get(srcPos);
                if (srcBeNbt != null) {
                    carrier.supp$setCarriedBlockEntityNbt(srcBeNbt);
                }
            }
            level.setBlockEntity(movingBe);
            BlockEntity verifyBe = level.getBlockEntity(dstPos);
            Supplementaries.LOGGER.info("[PulleyMover {}]   placed MOVING_PISTON at {} carrying {} -- BE present after setBlockEntity? {} (type {})",
                    level.isClientSide ? "client" : "server",
                    dstPos, srcState.getBlock(),
                    verifyBe != null,
                    verifyBe == null ? "null" : verifyBe.getClass().getSimpleName());
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos leftover : map.keySet()) {
            level.setBlock(leftover, air, 82);
        }

        for (Map.Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos p = entry.getKey();
            BlockState s = entry.getValue();
            s.updateIndirectNeighbourShapes(level, p, 2);
            air.updateNeighbourShapes(level, p, 2);
            air.updateIndirectNeighbourShapes(level, p, 2);
        }

        for (int k = destroyList.size() - 1; k >= 0; --k) {
            BlockPos pos = destroyList.get(k);
            level.getBlockState(pos).updateIndirectNeighbourShapes(level, pos, 2);
            level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        }

        for (int k = list.size() - 1; k >= 0; --k) {
            BlockPos pos = list.get(k);
            level.updateNeighborsAt(pos, originalStates.get(k).getBlock());
        }

        return true;
    }
}
