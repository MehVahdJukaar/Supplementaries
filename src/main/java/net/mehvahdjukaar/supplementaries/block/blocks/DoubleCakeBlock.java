package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Random;


import net.minecraft.block.AbstractBlock.Properties;

public class DoubleCakeBlock extends DirectionalCakeBlock {

    protected static final VoxelShape[] SHAPES_WEST = new VoxelShape[]{
            VoxelShapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(3, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(5, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(7, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(9, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(11, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(13, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_EAST = new VoxelShape[]{
            VoxelShapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(2, 8, 2, 13, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(2, 8, 2, 11, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 9, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 7, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 5, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 3, 15, 14),
                    box(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_SOUTH = new VoxelShape[]{
            VoxelShapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(2, 8, 2, 14, 15, 13),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(2, 8, 2, 14, 15, 11),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 14, 15, 9),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 14, 15, 7),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 14, 15, 5),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 2, 14, 15, 3),
                    box(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_NORTH= new VoxelShape[]{
            VoxelShapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(2, 8, 3, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or(box(2, 8, 5, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 7, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 9, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 11, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            VoxelShapes.or( box(2, 8, 13, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15))};

    public DoubleCakeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING)){
            default:
            case WEST:
                return SHAPES_WEST[state.getValue(BITES)];
            case EAST:
                return SHAPES_EAST[state.getValue(BITES)];
            case SOUTH:
                return SHAPES_SOUTH[state.getValue(BITES)];
            case NORTH:
                return SHAPES_NORTH[state.getValue(BITES)];
        }
    }
    //TODO: maybe merge this block with directional cake

    @Override
    public void removeSlice(BlockState state, BlockPos pos, IWorld world, Direction dir){
        int i = state.getValue(BITES);
        if (i < 6) {
            if (i == 0 && ServerConfigs.cached.DIRECTIONAL_CAKE) state = state.setValue(FACING, dir);
            world.setBlock(pos, state.setValue(BITES, i + 1), 3);
        } else {
            if(ServerConfigs.cached.DIRECTIONAL_CAKE){
                world.setBlock(pos, Registry.DIRECTIONAL_CAKE.get().defaultBlockState()
                        .setValue(FACING,state.getValue(FACING)).setValue(WATERLOGGED,state.getValue(WATERLOGGED)), 3);
            }
            else {
                world.setBlock(pos, Blocks.CAKE.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(CommonUtil.FESTIVITY.isStValentine()){
            if(rand.nextFloat()>0.8) {
                double d0 = (pos.getX() + 0.5 + (rand.nextFloat() - 0.5));
                double d1 = (pos.getY() + 0.5 + (rand.nextFloat() - 0.5));
                double d2 = (pos.getZ() + 0.5 + (rand.nextFloat() - 0.5));
                worldIn.addParticle(ParticleTypes.HEART, d0, d1, d2, 0, 0, 0);
            }
        }
    }

}