package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

public class FlagBlockTile extends TileEntity implements ITickableTileEntity {

    public float counter = 0;

    public FlagBlockTile() {
        super(Registry.FLAG_TILE.get());
    }


    public void tick() {
        if(this.world.isRemote) {
            //TODO:cache?
            float offset = 3f * (MathHelper.sin(this.pos.getX()) + MathHelper.sin(this.pos.getZ()));
            this.counter = this.world.getDayTime() + offset;
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

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }
}