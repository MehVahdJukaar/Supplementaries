package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;


public class BlackboardBlockTile extends TileEntity {

    public byte[][] pixels = new byte[16][16];

    private boolean isEditable = true;

    public BlackboardBlockTile() {
        super(Registry.BLACKBOARD_TILE.get());
        //Arrays.fill(pixels, Arrays.fill(new boolean[], false));
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                this.pixels[x][y] = 0;
            }
        }
    }

    public boolean isEmpty(){
        boolean flag = false;
        for (byte[] pixel : pixels) {
            for (byte b : pixel) {
                if (b != 0) {
                    flag = true;
                    break;
                }
            }
        }
        return !flag;
    }


    //TODO: optimize update packets
    @Override
    public void markDirty() {
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.markDirty();
    }

    //dont change name or it will crash with older saves
    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.pixels=new byte[16][16];
        for(int i = 0; i<16; i++) {
            byte[] b = compound.getByteArray("pixels_"+i);
            if(b.length==16)
                this.pixels[i] = b;
        }

    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        this.saveItemNBT(compound);
        return compound;
    }

    //doesn't save stuff it doesn't need. TODO: use this for update packet
    public CompoundNBT saveItemNBT(CompoundNBT compound){
        for(int i = 0; i<16; i++) {
            compound.putByteArray("pixels_"+i, this.pixels[i]);
        }
        return compound;
    }

    //not sure if needed
    public boolean getIsEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean isEditableIn) {
        this.isEditable = isEditableIn;
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
