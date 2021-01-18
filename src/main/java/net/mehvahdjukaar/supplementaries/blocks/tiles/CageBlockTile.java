package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.IMobHolder;
import net.mehvahdjukaar.supplementaries.common.MobHolder;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class CageBlockTile extends TileEntity implements ITickableTileEntity, IMobHolder {

    public MobHolder mobHolder;

    public CageBlockTile() {
        super(Registry.CAGE_TILE);
        this.mobHolder = new MobHolder(this.world,this.pos);
    }

    @Override
    public MobHolder getMobHolder(){return this.mobHolder;}

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public void onLoad() {
        this.mobHolder.setWorldAndPos(this.world,this.pos);
    }


    public void saveToNbt(ItemStack stack){
        CompoundNBT compound = new CompoundNBT();
        stack.setTagInfo("BlockEntityTag",write(compound));
    }

    //read==loadfromnbt, write=savetonbt.
    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        this.mobHolder.read(compound);


    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        this.mobHolder.write(compound);
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

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }

    @Override
    public void tick() {
        this.mobHolder.tick();
    }

    public boolean hasContent(){
        return !(this.mobHolder.isEmpty());
    }

}
