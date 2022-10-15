package net.mehvahdjukaar.supplementaries.integration.farmersdelight;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public abstract class TomatoLoggedBlock extends TomatoVineBlock {

    public TomatoLoggedBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void attemptRopeClimb(ServerLevel level, BlockPos pos, Random random) {
        if (random.nextFloat() < 0.3F) {
            BlockPos posAbove = pos.above();
            BlockState stateAbove = level.getBlockState(posAbove);
            boolean canClimb = stateAbove.is(ModTags.ROPES);
            if (canClimb) {
                int vineHeight;
                for (vineHeight = 1; level.getBlockState(pos.below(vineHeight)).getBlock() instanceof TomatoVineBlock; ++vineHeight) {
                }

                if (vineHeight < 3) {
                    BlockState toPlace;
                    if (stateAbove.is(ModRegistry.ROPE.get())) {
                        toPlace = FDCompatRegistry.ROPE_TOMATO.get().withPropertiesOf(stateAbove);
                    } else if (stateAbove.is(ModRegistry.STICK_BLOCK.get())) {
                        toPlace = FDCompatRegistry.STICK_TOMATOES.get().withPropertiesOf(stateAbove);
                    } else {
                        toPlace = ModBlocks.TOMATO_CROP.get().defaultBlockState().setValue(ROPELOGGED, true);
                    }
                    level.setBlockAndUpdate(posAbove, toPlace);
                }
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return (belowState.getBlock() instanceof TomatoVineBlock || super.canSurvive(state.setValue(ROPELOGGED, false), level, pos)) && this.hasGoodCropConditions(level, pos);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        super.playerDestroy(level, player, pos, state.setValue(ROPELOGGED, false), blockEntity, stack);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        this.playerWillDestroy(level, pos, state, player);
        return level.setBlock(pos, getInnerBlock().withPropertiesOf(state), level.isClientSide ? 11 : 3);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!state.canSurvive(level, pos)) {
            //we can't just break block or other ropes will react when instead we want to replace with another rope
            level.levelEvent(2001, pos, Block.getId(state));
            Block.dropResources(state, level, pos, null, null, ItemStack.EMPTY);

            level.setBlockAndUpdate(pos, getInnerBlock().withPropertiesOf(state));
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing,
                                  BlockState facingState, LevelAccessor world, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (!state.canSurvive(world, currentPos)) {
            world.scheduleTick(currentPos, this, 1);
        }
        return state;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        state = ModBlocks.TOMATO_CROP.get().withPropertiesOf(state);
        return state.getDrops(builder);
    }

    public abstract Block getInnerBlock();
}


