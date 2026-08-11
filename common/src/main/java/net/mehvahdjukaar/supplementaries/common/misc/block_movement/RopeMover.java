package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.cauldron.MovedFluidFiller;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Instant, one-block-at-a-time rope movement. Walks down a rope column, adds or removes a single
// rope at the end of it and shifts whatever hangs below by one slot, immediately: a plain setBlock,
// no animation and no structure resolving.
// This is the path for everything driven by hand or by an item: AbstractRopeBlock when a player
// right-clicks a rope with rope or shift-clicks to wind one up, RopeArrowEntity laying a column
// where it lands, and legacy-mode pulleys (pulley_block.continuous_retraction = false) via
// PulleyBlockTile.pullRope/releaseRope, including rotateIndirect where one pulley pokes another
// through a shared rope.
// PulleyMover is the other half: continuous-mode pulleys, where PulleyStructureResolver resolves the
// whole hanging structure first (sticky branching, pooled push budget, cooperating pulleys) and
// every block becomes a moving block entity sliding over several ticks. Use that one when the move
// should animate, carry more than the block directly under the rope, or be driven by redstone.
// Only isCorrectRope is shared between the two modes.
public class RopeMover {

    public static boolean addRopeDown(BlockPos pos, Level level, @Nullable Player player, InteractionHand hand, Block ropeBlock) {
        return addRope(pos, level, player, hand, ropeBlock, Direction.DOWN, Integer.MAX_VALUE);
    }

    public static boolean addRope(BlockPos pos, Level level, @Nullable Player player, InteractionHand hand,
                                  Block ropeBlock, Direction moveDir, int maxDist) {
        BlockState state = level.getBlockState(pos);
        if (maxDist <= 0) {
            return false;
        } else maxDist--;
        if (isCorrectRope(ropeBlock, state, moveDir)) {
            return addRope(pos.relative(moveDir), level, player, hand, ropeBlock, moveDir, maxDist);
        } else if (state.getBlock() instanceof PulleyBlock && level.getBlockEntity(pos) instanceof PulleyBlockTile te) {
            return te.rotateIndirect(player, hand, ropeBlock, moveDir, false);
        } else {
            return placeAndMove(player, hand, level, pos, moveDir, ropeBlock);
        }
    }

    public static boolean isCorrectRope(Block ropeBlock, BlockState state, Direction direction) {
        if (state.getBlock() instanceof ChainBlock && state.getValue(ChainBlock.AXIS) != direction.getAxis())
            return false;
        return ropeBlock == state.getBlock();
    }

    public static boolean placeAndMove(@Nullable Player player, InteractionHand hand, Level level,
                                       BlockPos originPos, Direction moveDir,
                                       //if null it will make the move operation override any target block
                                       @Nullable Block placeWhereItWas) {
        //check below can be moved down
        //check below block is replaceable
        BlockState originalState = level.getBlockState(originPos);
        BlockPos targetPos = originPos.relative(moveDir);
        BlockState targetState = level.getBlockState(targetPos);
        CompoundTag tileTag = null;

        boolean needsToPush = !originalState.canBeReplaced();
        if (needsToPush) {
            if (!targetState.canBeReplaced() && placeWhereItWas != null) return false;
            if (!isPushableByRopes(originalState, level, originPos, moveDir)) return false;
            // Blacklists (ours and Quark's) are enforced by isPushableByRopes, so anything reaching
            // here may be carried. Detaching also stops containers from spilling on removal.
            tileTag = PistonMovementHelper.captureAndDetachBlockEntity(level, originPos);
        }

        //gets clear state for new position
        FluidState originalFluid = level.getFluidState(originPos);

        //replace original block with air
        //place rope
        if (placeWhereItWas != null) {
            level.setBlock(originPos, originalFluid.createLegacyBlock(), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
            ItemStack stack = new ItemStack(placeWhereItWas);
            BlockPlaceContext context = new BlockPlaceContext(level, player, hand, stack, new BlockHitResult(Vec3.atCenterOf(originPos), moveDir.getOpposite(), originPos, false));
            if (stack.getItem() instanceof BlockItem bi) {
                InteractionResult placeResult = bi.place(context);
                if (placeResult == InteractionResult.PASS || placeResult == InteractionResult.FAIL) {
                    level.setBlock(originPos, originalState, Block.UPDATE_NONE);
                    return false;
                }

                if (!needsToPush) return true;
            }
        } else {
            level.setBlockAndUpdate(originPos, originalFluid.createLegacyBlock());
        }

        FluidState targetFluid = level.getFluidState(targetPos);

        boolean waterFluid = targetFluid.is(Fluids.WATER);
        boolean canHoldWater;
        if (originalState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            canHoldWater = originalState.is(ModTags.WATER_HOLDER);
            if (!canHoldWater) originalState = originalState.setValue(BlockStateProperties.WATERLOGGED, waterFluid);
        } else {
            originalState = MovedFluidFiller.fillIfMovedIntoFluid(originalState, level, targetPos, targetFluid);
        }

        //clear existing block to new position
        originalState = Block.updateFromNeighbourShapes(originalState, level, targetPos);
        level.setBlockAndUpdate(targetPos, originalState);
        if (tileTag != null) {
            PistonMovementHelper.restoreBlockEntity(level, targetPos, originalState, tileTag);
        }
        //populate any block-entity data for cauldron-like blocks moved into a fluid
        MovedFluidFiller.applyPostPlacement(level, targetPos, targetFluid);

        return true;
    }

    public static boolean removeRopeDown(BlockPos pos, Level level, Block ropeBlock) {
        return removeRope(pos, level, ropeBlock, Direction.DOWN, Integer.MAX_VALUE);
    }

    public static boolean removeRope(BlockPos pos, Level level, Block ropeBlock, Direction moveUpDir, int maxDist) {
        if (maxDist <= 0) {
            return false;
        } else maxDist--;
        BlockState state = level.getBlockState(pos);
        if (isCorrectRope(ropeBlock, state, moveUpDir)) {
            return removeRope(pos.relative(moveUpDir), level, ropeBlock, moveUpDir, maxDist);

        } else if (state.getBlock() instanceof PulleyBlock
                && level.getBlockEntity(pos) instanceof PulleyBlockTile te && !te.isEmpty()) {
            return te.rotateIndirect(null, InteractionHand.MAIN_HAND, ropeBlock, moveUpDir, true);
        } else {
            BlockPos up = pos.relative(moveUpDir.getOpposite());
            if ((level.getBlockState(up).getBlock() != ropeBlock)) return false;
            if (!placeAndMove(null, InteractionHand.MAIN_HAND, level, pos, moveUpDir.getOpposite(), null)) {
                level.setBlockAndUpdate(up, level.getFluidState(up).createLegacyBlock());
            }
            return true;
        }
    }


    // Whether a rope may drag this block along: the shared mover rules, plus pulleys are never
    // dragged, blocks whose partner wouldn't come along (ROPE_PUSH_BLACKLIST) are refused, and
    // ROPE_HANG_TAG blocks ride along vertically whatever their push reaction.
    public static boolean isPushableByRopes(BlockState state, Level level, BlockPos pos, Direction moveDir) {
        if (state.getBlock() instanceof PulleyBlock) return false; //could be in the tag but easier for addons like this
        if (state.is(ModTags.ROPE_PUSH_BLACKLIST)) return false;
        if (!state.isSolid()) return false;
        if (moveDir.getAxis().isVertical() && state.is(ModTags.ROPE_HANG_TAG)) {
            return true;
        }
        // Never destroys: the solidity check above already rejected everything a rope could break.
        return PistonMovementHelper.isPushableByOurMovers(state, level, pos, moveDir, false, moveDir);
    }
}
