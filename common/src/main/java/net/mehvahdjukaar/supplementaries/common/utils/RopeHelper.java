package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RopeHelper {

    public static boolean addRopeDown(BlockPos pos, Level level, @Nullable Player player, InteractionHand hand, Block ropeBlock) {
        return addRope(pos, level, player, hand, ropeBlock, Direction.DOWN, true, Integer.MAX_VALUE);
    }

    public static boolean addRope(BlockPos pos, Level level, @Nullable Player player, InteractionHand hand,
                                  Block ropeBlock, Direction moveDir, boolean canPush, int maxDist) {
        BlockState state = level.getBlockState(pos);
        if (maxDist <= 0) {
            return false;
        } else maxDist--;
        if (isCorrectRope(ropeBlock, state, moveDir)) {
            return addRope(pos.relative(moveDir), level, player, hand, ropeBlock, moveDir, canPush, maxDist);
        } else if (state.is(ModRegistry.PULLEY_BLOCK.get()) && level.getBlockEntity(pos) instanceof PulleyBlockTile te) {
            return te.rotateIndirect(player, hand, ropeBlock, moveDir, false);
        } else {
            return tryPlaceAndMove(player, hand, level, pos, ropeBlock, moveDir, canPush);
        }
    }

    public static boolean isCorrectRope(Block ropeBlock, BlockState state, Direction direction) {
        if (state.getBlock() instanceof ChainBlock && state.getValue(ChainBlock.AXIS) != direction.getAxis())
            return false;
        return ropeBlock == state.getBlock();
    }

    public static boolean tryPlaceAndMove(@Nullable Player player, InteractionHand hand, Level world,
                                          BlockPos pos, Block ropeBlock, Direction moveDir, boolean canPush) {
        ItemStack stack = new ItemStack(ropeBlock);

        //TODO: maybe pass fake player here
        BlockPlaceContext context = new BlockPlaceContext(world, player, hand, stack, new BlockHitResult(Vec3.atCenterOf(pos), moveDir.getOpposite(), pos, false));
        if (!context.canPlace()) {
            //checks if block below this is hollow
            BlockPos downPos = pos.relative(moveDir);
            //try move block down
            if (!canPush || !(world.getBlockState(downPos).canBeReplaced()
                    && tryMove(pos, downPos, world))) return false;
            context = new BlockPlaceContext(world, player, hand, stack, new BlockHitResult(Vec3.atCenterOf(pos), moveDir.getOpposite(), pos, false));
        }

        //place rope
        BlockState state = ItemsUtil.getPlacementState(context, ropeBlock);
        if (state == null) return false;
        if (state == world.getBlockState(context.getClickedPos())) return false;
        if (world.setBlock(context.getClickedPos(), state, 11)) {
            if (player != null) {
                BlockState placedState = world.getBlockState(context.getClickedPos());
                Block block = placedState.getBlock();
                if (block == state.getBlock()) {
                    block.setPlacedBy(world, context.getClickedPos(), placedState, player, stack);
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, context.getClickedPos(), stack);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isBlockMovable(BlockState state, Level level, BlockPos pos) {
        //hardcoded stuff from vanilla
        if(state.getBlock() instanceof PulleyBlock)return false;
        return (!state.isAir() && !state.is(Blocks.OBSIDIAN) && !state.is(Blocks.SPAWNER) &&
                !state.is(Blocks.CRYING_OBSIDIAN) && !state.is(Blocks.RESPAWN_ANCHOR))
                && state.getDestroySpeed(level, pos) != -1;
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

        } else if (state.is(ModRegistry.PULLEY_BLOCK.get())
                && level.getBlockEntity(pos) instanceof PulleyBlockTile te && !te.isEmpty()) {
            return te.rotateIndirect(null, InteractionHand.MAIN_HAND, ropeBlock, moveUpDir, true);
            //TODO: rrmove thse param
        } else {
            //if (dist == 0) return false;
            BlockPos up = pos.relative(moveUpDir.getOpposite());
            if ((level.getBlockState(up).getBlock() != ropeBlock)) return false;
            FluidState fromFluid = level.getFluidState(up);
            boolean water = (fromFluid.getType() == Fluids.WATER && fromFluid.isSource());
            level.setBlockAndUpdate(up, water ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
            tryMove(pos, up, level);
            return true;
        }
    }


    //TODO: fix order of operations to allow pulling down lanterns
    @SuppressWarnings("ConstantConditions")
    private static boolean tryMove(BlockPos fromPos, BlockPos toPos, Level world) {
        if (toPos.getY() < world.getMinBuildHeight() || toPos.getY() > world.getMaxBuildHeight()) return false;
        BlockState state = world.getBlockState(fromPos);

        PushReaction push = state.getPistonPushReaction();

        if (isBlockMovable(state, world, fromPos) &&
                (
                        ((push == PushReaction.NORMAL || (toPos.getY() < fromPos.getY() && push == PushReaction.PUSH_ONLY))
                                && state.canSurvive(world, toPos)) || (state.is(ModTags.ROPE_HANG_TAG))
                )
        ) {

            BlockEntity tile = world.getBlockEntity(fromPos);
            if (tile != null) {
                //moves everything if quark is not enabled. bad :/ install quark guys
                if (CompatHandler.QUARK && !QuarkCompat.canMoveBlockEntity(state)) {
                    return false;
                } else {
                    tile.setRemoved();
                }
            }

            //gets refreshTextures state for new position

            Fluid fluidState = world.getFluidState(toPos).getType();
            boolean waterFluid = fluidState == Fluids.WATER;
            boolean canHoldWater = false;
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                canHoldWater = state.is(ModTags.WATER_HOLDER);
                if (!canHoldWater) state = state.setValue(BlockStateProperties.WATERLOGGED, waterFluid);
            } else if (state.getBlock() instanceof AbstractCauldronBlock) {
                if (waterFluid && state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)) {
                    state = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
                }
                if (fluidState == Fluids.LAVA && state.is(Blocks.CAULDRON) || state.is(Blocks.LAVA_CAULDRON)) {
                    state = Blocks.LAVA_CAULDRON.defaultBlockState();
                }
                //TODO: amendmnts here
            }


            FluidState fromFluid = world.getFluidState(fromPos);
            boolean leaveWater = (fromFluid.getType() == Fluids.WATER && fromFluid.isSource()) && !canHoldWater;
            world.setBlockAndUpdate(fromPos, leaveWater ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());

            //refreshTextures existing block block to new position
            BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            world.setBlockAndUpdate(toPos, newState);
            if (tile != null) {
                CompoundTag tag = tile.saveWithoutMetadata();
                BlockEntity te = world.getBlockEntity(toPos);
                if (te != null) {
                    te.load(tag);
                }
            }
            //world.notifyNeighborsOfStateChange(toPos, state.getBlock());
            world.neighborChanged(toPos, state.getBlock(), toPos);
            return true;
        }
        return false;
    }
}
