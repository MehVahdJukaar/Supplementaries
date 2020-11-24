package net.mehvahdjukaar.supplementaries.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class CandelabraBlock extends HorizontalFaceBlock {
    public CandelabraBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACE, HORIZONTAL_FACING);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(rand.nextFloat()>0.99)return;
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
                xm = pos.getX() + 0.51D;
                ym = pos.getY() + 1D;
                zm = pos.getZ() + 0.5D;
                xl = pos.getX() + 0.5D - dir.getXOffset()*0.3125D;
                yl = pos.getY() + 0.9375D;
                zl = pos.getZ() + 0.5D - dir.getZOffset()*0.3125D;
                xr = pos.getX() + 0.5D + dir.getXOffset()*0.3125D;
                zr = pos.getZ() + 0.5D + dir.getZOffset()*0.3125D;
                break;

        }
        worldIn.addParticle(ParticleTypes.FLAME, xm, ym, zm, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xl, yl, zl, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xr, yl, zr, 0, 0, 0);

    }
}
