package net.mehvahdjukaar.supplementaries.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class CandelabraBlock extends HorizontalFaceBlock {
    protected static final VoxelShape SHAPE_FLOOR = Block.makeCuboidShape(5D, 0D, 5D, 11D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_NORTH = Block.makeCuboidShape(5D, 0D, 10D, 11D, 15D, 16D);
    protected static final VoxelShape SHAPE_WALL_SOUTH = Block.makeCuboidShape(5D, 0D, 0D, 11D, 15D, 6D);
    protected static final VoxelShape SHAPE_WALL_WEST = Block.makeCuboidShape(10D, 0D, 5D, 16D, 15D, 11D);
    protected static final VoxelShape SHAPE_WALL_EAST = Block.makeCuboidShape(0D, 0D, 5D, 6D, 15D, 11D);
    protected static final VoxelShape SHAPE_CEILING = Block.makeCuboidShape(5D, 3D, 5D, 11D, 16D, 11D);

    public CandelabraBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACE, HORIZONTAL_FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch(state.get(FACE)){
            default:
            case FLOOR:
                return SHAPE_FLOOR;
            case WALL:
                switch (state.get(HORIZONTAL_FACING)){
                    default:
                    case NORTH:
                        return SHAPE_WALL_NORTH;
                    case SOUTH:
                         return SHAPE_WALL_SOUTH;
                    case WEST:
                        return SHAPE_WALL_WEST;
                    case EAST:
                        return SHAPE_WALL_EAST;
                }
            case CEILING:
                return SHAPE_CEILING;
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        if(state.get(FACE)==AttachFace.FLOOR){
            return hasEnoughSolidSide(worldIn, pos.down(), Direction.UP);
        }
        else if(state.get(FACE)==AttachFace.CEILING){
            return hasEnoughSolidSide(worldIn, pos.up(), Direction.DOWN);
        }
        return super.isValidPosition(state,worldIn,pos);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        Direction dir = stateIn.get(HORIZONTAL_FACING);
        double xm,ym,zm,xl,yl,zl,xr,zr;
        switch(stateIn.get(FACE)){
            default:
            case FLOOR:
                dir=dir.rotateY();
                xm = pos.getX() + 0.5D;
                ym = pos.getY() + 1D;
                zm = pos.getZ() + 0.5D;
                xl = pos.getX() + 0.5D - dir.getXOffset()*0.3125D;
                yl = pos.getY() + 0.9375D;
                zl = pos.getZ() + 0.5D - dir.getZOffset()*0.3125D;
                xr = pos.getX() + 0.5D + dir.getXOffset()*0.3125D;
                zr = pos.getZ() + 0.5D + dir.getZOffset()*0.3125D;
                break;
            case WALL:
                double xoff = -dir.getXOffset()*0.25D;
                double zoff = -dir.getZOffset()*0.25D;
                dir=dir.rotateY();
                xm = pos.getX() + 0.5D + xoff;
                ym = pos.getY() + 1.0625D;
                zm = pos.getZ() + 0.5D + zoff;
                xl = pos.getX() + 0.5D + xoff- dir.getXOffset()*0.3125D;
                yl = pos.getY() + 1D;
                zl = pos.getZ() + 0.5D + zoff - dir.getZOffset()*0.3125D;
                xr = pos.getX() + 0.5D + xoff + dir.getXOffset()*0.3125D;
                zr = pos.getZ() + 0.5D + zoff + dir.getZOffset()*0.3125D;
                break;
            case CEILING:
                dir=dir.rotateY();
                //high
                xm = pos.getX() + 0.5D + dir.getZOffset()*0.3125D;
                zm = pos.getZ() + 0.5D - dir.getXOffset()*0.3125D;
                ym = pos.getY() + 0.875;//0.9375D;
                //2 medium
                xl = pos.getX() + 0.5D + dir.getXOffset()*0.3125D;
                zl = pos.getZ() + 0.5D + dir.getZOffset()*0.3125D;
                xr = pos.getX() + 0.5D - dir.getZOffset()*0.3125D;
                zr = pos.getZ() + 0.5D + dir.getXOffset()*0.3125D;
                yl = pos.getY() + 0.8125;

                double xs = pos.getX() + 0.5D - dir.getXOffset()*0.3125D;
                double zs = pos.getZ() + 0.5D - dir.getZOffset()*0.3125D;
                double ys = pos.getY() + 0.75;
                worldIn.addParticle(ParticleTypes.FLAME, xs, ys, zs, 0, 0, 0);
                break;

        }
        worldIn.addParticle(ParticleTypes.FLAME, xm, ym, zm, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xl, yl, zl, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xr, yl, zr, 0, 0, 0);

    }
}
