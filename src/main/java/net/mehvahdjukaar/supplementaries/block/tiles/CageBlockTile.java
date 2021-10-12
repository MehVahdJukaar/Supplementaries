package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

public class CageBlockTile extends TileEntity implements ITickableTileEntity, IMobContainerProvider {

    @Nonnull
    public MobContainer mobContainer;

    public CageBlockTile() {
        super(ModRegistry.CAGE_TILE.get());
        AbstractMobContainerItem item = ((AbstractMobContainerItem) ModRegistry.CAGE_ITEM.get());
        this.mobContainer = new MobContainer(item.getMobContainerWidth(), item.getMobContainerHeight(), this.level, this.worldPosition);
    }

    @Override
    public double getViewDistance() {
        return 80;
    }

    public void saveToNbt(ItemStack stack){
        CompoundNBT compound = new CompoundNBT();
        stack.addTagElement("BlockEntityTag",save(compound));
    }

    //ugly but the world is given as null when loading
    @Override
    public void onLoad() {
        super.onLoad();
        this.mobContainer.setWorldAndPos(level, worldPosition);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.mobContainer.load(compound);
        if(this.level != null){
            //onLoad();
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        this.mobContainer.save(compound);
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

    @Override
    public MobContainer getMobContainer() {
        return this.mobContainer;
    }

    @Override
    public Direction getDirection() {
        return this.getBlockState().getValue(ClockBlock.FACING);
    }

    @Override
    public void tick() {
        this.mobContainer.tick();
    }

}
