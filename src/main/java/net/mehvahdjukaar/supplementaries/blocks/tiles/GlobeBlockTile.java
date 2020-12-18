package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.GlobeBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class GlobeBlockTile extends TileEntity implements ITickableTileEntity {
    public float yaw = 0;
    public float prevYaw = 0;
    public int face = 0;

    public GlobeBlockTile() {
        super(Registry.GLOBE_TILE);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.face = compound.getInt("face");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("face",this.face);
        return compound;
    }

    public void spin(){
        this.face=(this.face-=90)%360;
        this.yaw=this.yaw+360+90;
        this.markDirty();
    }

    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.spin();
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
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

    @Override
    public void tick() {
        if(this.world.isRemote){
            this.prevYaw=this.yaw;
            if(this.yaw!=0)this.yaw=Math.max(0,(this.yaw*0.94f) -0.7f);
        }
    }

    public Direction getDirection(){
        return this.getBlockState().get(GlobeBlock.FACING);
    }
}