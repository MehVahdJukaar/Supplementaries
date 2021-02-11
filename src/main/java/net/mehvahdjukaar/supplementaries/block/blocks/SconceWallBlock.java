package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class SconceWallBlock extends SconceBlock {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    //TODO: make map for other blocks
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, makeCuboidShape(6D, 2.0D, 10D, 10D, 13.0D, 16.0D),
            Direction.SOUTH, makeCuboidShape(6D, 2.0D, 0.0D, 10D, 13.0D, 6D),
            Direction.WEST, makeCuboidShape(10D, 2.0D, 6D, 16.0D, 13.0D, 10D),
            Direction.EAST, makeCuboidShape(0.0D, 2.0D, 6D, 6D, 13.0D, 10D)));

    public SconceWallBlock(Properties properties, IParticleData particleData) {
        super(properties, particleData);
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(LIT, true));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return facing.getOpposite() == stateIn.get(FACING) && !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : stateIn;
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(stateIn.get(LIT)){
            Direction direction = stateIn.get(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.7D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.25D * (double) direction1.getXOffset(), d1 + 0.15D, d2 + 0.25D * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData, d0 + 0.25D * (double) direction1.getXOffset(), d1 + 0.15D, d2 + 0.25D * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return blockstate.isSolidSide(worldIn, blockpos, direction);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        BlockState blockstate = this.getDefaultState();
        IWorldReader iworldreader = context.getWorld();
        BlockPos blockpos = context.getPos();
        Direction[] adirection = context.getNearestLookingDirections();

        for(Direction direction : adirection) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.with(FACING, direction1);
                if (blockstate.isValidPosition(iworldreader, blockpos)) {
                    return blockstate.with(WATERLOGGED, flag).with(LIT,!flag);
                }
            }
        }
        return null;
    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT,FACING,WATERLOGGED);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

}
