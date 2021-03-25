package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

public class FlagBlockTile extends TileEntity implements ITickableTileEntity {

    public final float offset = 3f * (MathHelper.sin(this.worldPosition.getX()) + MathHelper.sin(this.worldPosition.getZ()));
    public float counter = 0;
    public FlagBlockTile() {
        super(Registry.FLAG_TILE.get());
    }

    @Override
    public double getViewDistance() {
        return 96;
    }

    public void tick() {

        if(!this.level.isClientSide){
            int b = 1;
        }
        else{
            int c = 1;
        }

        if(this.level.isClientSide) {
            //TODO:cache?
            //TODO: make long or float. wind vane too
            this.counter = (this.level.getGameTime()%24000)+offset;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        Direction dir = this.getDirection();
        return new AxisAlignedBB(0.25,0, 0.25, 0.75, 1, 0.75).expandTowards(
                dir.getStepX()*1.35f,0,dir.getStepZ()*1.35f).move(this.worldPosition);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(FlagBlock.FACING);
    }

}