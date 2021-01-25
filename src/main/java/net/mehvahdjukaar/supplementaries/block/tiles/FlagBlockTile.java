package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

public class FlagBlockTile extends TileEntity implements ITickableTileEntity {

    public final float offset = 3f * (MathHelper.sin(this.pos.getX()) + MathHelper.sin(this.pos.getZ()));
    public float counter = 0;
    public FlagBlockTile() {
        super(Registry.FLAG_TILE.get());
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 96;
    }

    public void tick() {
        if(this.world.isRemote) {
            //TODO:cache?
            //TODO: make long or float. wind vane too
            this.counter = (this.world.getGameTime()%24000)+offset;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        Direction dir = this.getDirection();
        return new AxisAlignedBB(0.25,0, 0.25, 0.75, 1, 0.75).expand(
                dir.getXOffset()*1.35f,0,dir.getZOffset()*1.35f).offset(this.pos);
    }

    public Direction getDirection() {
        return this.getBlockState().get(FlagBlock.FACING);
    }

}