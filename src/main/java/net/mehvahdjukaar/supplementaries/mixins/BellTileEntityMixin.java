package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.util.IBellConnection;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.BellTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BellTileEntity.class)
public abstract class BellTileEntityMixin extends TileEntity  implements IBellConnection {
    public BellConnection connection = BellConnection.NONE;


    public BellTileEntityMixin(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }


    @Override
    public BellConnection getConnected() {
        return connection;
    }

    @Override
    public void setConnected(BellConnection con) {
        this.connection=con;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        //not needed but since I keep getting reports lets do this
        try {
            if (this.connection != null)
                compound.putInt("Connection", this.connection.ordinal());
        }catch (Exception ignored){}
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        try {
            if(compound.contains("Connection"))
                this.connection = BellConnection.values()[compound.getInt("Connection")];
        }catch (Exception ignored){
            this.connection = BellConnection.NONE;
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
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos);
    }
}