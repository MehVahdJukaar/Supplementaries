package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;


public class DoubleCakeBlock extends DirectionalCakeBlock {

    protected static final VoxelShape[] SHAPES_WEST = new VoxelShape[]{
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(3, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(5, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(7, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(9, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(11, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(13, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_EAST = new VoxelShape[]{
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 13, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 11, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 9, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 7, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 5, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 3, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_SOUTH = new VoxelShape[]{
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 14, 15, 13),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 14, 15, 11),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 14, 15, 9),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 14, 15, 7),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 14, 15, 5),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 2, 14, 15, 3),
                    makeCuboidShape(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_NORTH= new VoxelShape[]{
            VoxelShapes.or(makeCuboidShape(2, 8, 2, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(2, 8, 3, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(makeCuboidShape(2, 8, 5, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 7, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 9, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 11, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( makeCuboidShape(2, 8, 13, 14, 15, 14),
                    makeCuboidShape(1, 0, 1, 15, 8, 15))};

    public DoubleCakeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)){
            default:
            case WEST:
                return SHAPES_WEST[state.get(BITES)];
            case EAST:
                return SHAPES_EAST[state.get(BITES)];
            case SOUTH:
                return SHAPES_SOUTH[state.get(BITES)];
            case NORTH:
                return SHAPES_NORTH[state.get(BITES)];
        }
    }
    //TODO: maybe merge this block with directional cake

    @Override
    public void removeSlice(BlockState state, BlockPos pos, IWorld world, Direction dir){
        int i = state.get(BITES);
        if (i < 6) {
            if (i == 0 && ServerConfigs.cached.DIRECTIONAL_CAKE) state = state.with(FACING, dir);
            world.setBlockState(pos, state.with(BITES, i + 1), 3);
        } else {
            if(ServerConfigs.cached.DIRECTIONAL_CAKE){
                world.setBlockState(pos, Registry.DIRECTIONAL_CAKE.get().getDefaultState()
                        .with(FACING,state.get(FACING)).with(WATERLOGGED,state.get(WATERLOGGED)), 3);
            }
            else {
                world.setBlockState(pos, Blocks.CAKE.getDefaultState(), 3);
            }
        }
    }

}