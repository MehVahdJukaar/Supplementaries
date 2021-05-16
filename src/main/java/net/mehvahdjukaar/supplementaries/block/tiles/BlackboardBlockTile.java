package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
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
    public void setChanged() {
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.setChanged();
    }

    //dont change name or it will crash with older saves
    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.pixels=new byte[16][16];
        for(int i = 0; i<16; i++) {
            byte[] b = compound.getByteArray("pixels_"+i);
            if(b.length==16)
                this.pixels[i] = b;
        }

    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
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
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    public Direction getDirection(){
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    public float getYaw() {
        return -this.getDirection().toYRot();
    }
}
