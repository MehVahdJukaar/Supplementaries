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

public class SconceLeverBlock extends SconceWallBlock{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public SconceLeverBlock(Properties properties, Supplier<BasicParticleType> particleData) {
        super(properties, particleData);
        this.setDefaultState(this.stateContainer.getBaseState().with(POWERED,false)
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(LIT, true));
    }

    //need to update neighbours too
    @Override
    public void onChange(BlockState state, IWorld world, BlockPos pos) {
        if(world instanceof World)
            this.updateNeighbors(state, (World) world, pos);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ActionResultType result = super.onBlockActivated(state,worldIn,pos,player,handIn,hit);
        if(result.isSuccessOrConsume()) {
            this.updateNeighbors(state, worldIn, pos);
            return result;
        }
        if (worldIn.isRemote) {
            state.func_235896_a_(POWERED);
            return ActionResultType.SUCCESS;
        } else {
            BlockState blockstate = this.setPowered(state, worldIn, pos);
            float f = blockstate.get(POWERED) ? 0.6F : 0.5F;
            worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
            return ActionResultType.CONSUME;
        }
    }
    public BlockState setPowered(BlockState state, World world, BlockPos pos) {
        state = state.func_235896_a_(POWERED);
        world.setBlockState(pos, state, 3);
        this.updateNeighbors(state, world, pos);
        return state;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.isIn(newState.getBlock())) {
            if (state.get(POWERED)) {
                this.updateNeighbors(state, worldIn, pos);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED)^!blockState.get(LIT) ? 15 : 0;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED)^!blockState.get(LIT)  && getFacing(blockState) == side ? 15 : 0;
    }
    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.notifyNeighborsOfStateChange(pos, this);
        world.notifyNeighborsOfStateChange(pos.offset(getFacing(state).getOpposite()), this);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWERED);
    }

    protected static Direction getFacing(BlockState state) {
        return state.get(FACING);
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(!stateIn.get(POWERED)) {
            super.animateTick(stateIn, worldIn, pos, rand);
        }
        else if(stateIn.get(LIT)){
            Direction direction = stateIn.get(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.65D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.125D * (double) direction1.getXOffset(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData.get(), d0 + 0.125D * (double) direction1.getXOffset(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
        }
    }
}