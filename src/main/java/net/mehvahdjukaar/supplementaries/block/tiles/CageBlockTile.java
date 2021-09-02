package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.block.util.IMobHolder;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
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
        super(ModRegistry.CAGE_TILE.get());
        this.mobHolder = new MobHolder(this.level,this.worldPosition);
    }

    @Override
    public MobHolder getMobHolder(){return this.mobHolder;}


    @Override
    public double getViewDistance() {
        return 80;
    }

    @Override
    public void onLoad() {
        this.mobHolder.setWorldAndPos(this.level,this.worldPosition);
    }

    public void saveToNbt(ItemStack stack){
        CompoundNBT compound = new CompoundNBT();
        stack.addTagElement("BlockEntityTag",save(compound));
    }

    //read==loadfromnbt, write=savetonbt.
    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.mobHolder.read(compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        this.mobHolder.write(compound);
        return compound;
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

    public Direction getDirection() {
        return this.getBlockState().getValue(ClockBlock.FACING);
    }

    @Override
    public void tick() {
        this.mobHolder.tick();
    }

    public boolean hasContent(){
        return !(this.mobHolder.isEmpty());
    }

}
