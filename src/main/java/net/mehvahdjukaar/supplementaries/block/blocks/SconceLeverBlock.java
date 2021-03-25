package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock.Properties;

public class SconceLeverBlock extends SconceWallBlock{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public SconceLeverBlock(Properties properties, Supplier<BasicParticleType> particleData) {
        super(properties, particleData);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED,false)
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(LIT, true));
    }

    //need to update neighbours too
    @Override
    public void onChange(BlockState state, IWorld world, BlockPos pos) {
        if(world instanceof World)
            this.updateNeighbors(state, (World) world, pos);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ActionResultType result = super.use(state,worldIn,pos,player,handIn,hit);
        if(result.consumesAction()) {
            this.updateNeighbors(state, worldIn, pos);
            return result;
        }
        if (worldIn.isClientSide) {
            state.cycle(POWERED);
            return ActionResultType.SUCCESS;
        } else {
            BlockState blockstate = this.setPowered(state, worldIn, pos);
            float f = blockstate.getValue(POWERED) ? 0.6F : 0.5F;
            worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
            return ActionResultType.CONSUME;
        }
    }
    public BlockState setPowered(BlockState state, World world, BlockPos pos) {
        state = state.cycle(POWERED);
        world.setBlock(pos, state, 3);
        this.updateNeighbors(state, world, pos);
        return state;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            if (state.getValue(POWERED)) {
                this.updateNeighbors(state, worldIn, pos);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED)^!blockState.getValue(LIT) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED)^!blockState.getValue(LIT)  && getFacing(blockState) == side ? 15 : 0;
    }
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        world.updateNeighborsAt(pos.relative(getFacing(state).getOpposite()), this);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    protected static Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(!stateIn.getValue(POWERED)) {
            super.animateTick(stateIn, worldIn, pos, rand);
        }
        else if(stateIn.getValue(LIT)){
            Direction direction = stateIn.getValue(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.65D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.125D * (double) direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData.get(), d0 + 0.125D * (double) direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}