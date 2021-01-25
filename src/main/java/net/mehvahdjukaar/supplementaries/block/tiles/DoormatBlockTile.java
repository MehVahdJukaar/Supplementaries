package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolder;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants;

public class DoormatBlockTile extends TileEntity implements ITextHolder {
    public static final int MAXLINES = 3;

    public TextHolder textHolder;

    public DoormatBlockTile() {
        super(Registry.DOORMAT_TILE.get());
        this.textHolder = new TextHolder(MAXLINES);
    }

    @Override
    public TextHolder getTextHolder(){return this.textHolder;}

    @Override
    public void markDirty() {
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.markDirty();
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.textHolder.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        this.textHolder.write(compound);
        return compound;
    }

    public Direction getDirection(){
        return this.getBlockState().get(DoormatBlock.FACING);
    }

    //TODO: optimize this two methods to send only what's needed
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