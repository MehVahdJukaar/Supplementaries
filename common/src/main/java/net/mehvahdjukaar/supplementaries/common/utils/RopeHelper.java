package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.integration.AmendmentsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RopeHelper {

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

            BlockEntity tile = level.getBlockEntity(originPos);
            if (tile != null) {
                if (CompatHandler.QUARK && !QuarkCompat.canMoveBlockEntity(originalState)) {
                    return false;
                } else {
                    tile.setRemoved();
                }
                tileTag = tile.saveWithoutMetadata();
            }
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

        //remove block below to make space
        //   level.destroyBlock(targetPos, false);
        FluidState targetFluid = level.getFluidState(targetPos);

        boolean waterFluid = targetFluid.is(Fluids.WATER);
        boolean canHoldWater;
        if (originalState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            canHoldWater = originalState.is(ModTags.WATER_HOLDER);
            if (!canHoldWater) originalState = originalState.setValue(BlockStateProperties.WATERLOGGED, waterFluid);
        } else if (originalState.getBlock() instanceof AbstractCauldronBlock) {
            if (waterFluid && originalState.is(Blocks.CAULDRON) || originalState.is(Blocks.WATER_CAULDRON)) {
                originalState = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
            } else if (targetFluid.is(Fluids.LAVA) && originalState.is(Blocks.CAULDRON) || originalState.is(Blocks.LAVA_CAULDRON)) {
                originalState = Blocks.LAVA_CAULDRON.defaultBlockState();
            } else if (CompatHandler.AMENDMENTS) {
                //TODO:this isnt correct actually it needs to set tile after
                originalState = AmendmentsCompat.fillCauldronWithFluid(level, targetPos, originalState, targetFluid);
            }
        }

        //clear existing block to new position
        originalState = Block.updateFromNeighbourShapes(originalState, level, targetPos);
        level.setBlockAndUpdate(targetPos, originalState);
        if (tileTag != null) {
            BlockEntity te = level.getBlockEntity(targetPos);
            if (te != null) {
                te.load(tileTag);
            }
        }

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
            //if (dist == 0) return false;
            BlockPos up = pos.relative(moveUpDir.getOpposite());
            if ((level.getBlockState(up).getBlock() != ropeBlock)) return false;
            if (!placeAndMove(null, InteractionHand.MAIN_HAND, level, pos, moveUpDir.getOpposite(), null)) {
                level.setBlockAndUpdate(up, level.getFluidState(up).createLegacyBlock());
            }
            return true;
        }
    }


    public static boolean isPushableByRopes(BlockState state, Level level, BlockPos pos, Direction moveDir) {
        //hardcoded stuff from vanilla
        if (state.getBlock() instanceof PulleyBlock) return false; //could be in the tag but easier for addons like this
        if (state.is(ModTags.ROPE_PUSH_BLACKLIST)) return false;
        if (!state.isSolid()) return false;
        if (moveDir.getAxis().isVertical() && state.is(ModTags.ROPE_HANG_TAG)) {
            return true;
        }
        boolean couldBreak = !state.isSolid();
        return isPushable(state, level, pos, moveDir, couldBreak, moveDir);
    }


    //same as PistonBaseBlock.isPushable but ignores some stuff like Block Entities
    private static boolean isPushable(BlockState state, Level level, BlockPos pos, Direction movementDirection, boolean allowDestroy, Direction pistonFacing) {
        if (pos.getY() >= level.getMinBuildHeight() && pos.getY() <= level.getMaxBuildHeight() - 1 && level.getWorldBorder().isWithinBounds(pos)) {
            if (state.isAir()) {
                return true;
            } else if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.CRYING_OBSIDIAN) && !state.is(Blocks.RESPAWN_ANCHOR) && !state.is(Blocks.REINFORCED_DEEPSLATE)) {
                if (movementDirection == Direction.DOWN && pos.getY() == level.getMinBuildHeight()) {
                    return false;
                } else if (movementDirection == Direction.UP && pos.getY() == level.getMaxBuildHeight() - 1) {
                    return false;
                } else {
                    if (!state.is(Blocks.PISTON) && !state.is(Blocks.STICKY_PISTON)) {
                        if (state.getDestroySpeed(level, pos) == -1.0F) {
                            return false;
                        }

                        switch (state.getPistonPushReaction()) {
                            case BLOCK -> {
                                return false;
                            }
                            case DESTROY -> {

                                return allowDestroy;
                            }
                            case PUSH_ONLY -> {
                                return movementDirection == pistonFacing;
                            }
                        }
                    } else if (state.getValue(PistonBaseBlock.EXTENDED)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
