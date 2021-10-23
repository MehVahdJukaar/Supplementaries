package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

public class CageBlockTile extends BlockEntity implements TickableBlockEntity, IMobContainerProvider {

    @Nonnull
    public MobContainer mobContainer;

    public CageBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.CAGE_TILE.get(), pos, state);
        AbstractMobContainerItem item = ((AbstractMobContainerItem) ModRegistry.CAGE_ITEM.get());
        this.mobContainer = new MobContainer(item.getMobContainerWidth(), item.getMobContainerHeight(), this.level, this.worldPosition);
    }

    @Override
    public double getViewDistance() {
        return 80;
    }

    public void saveToNbt(ItemStack stack){
        CompoundTag compound = new CompoundTag();
        stack.addTagElement("BlockEntityTag",save(compound));
    }

    //ugly but the world is given as null when loading
    @Override
    public void onLoad() {
        super.onLoad();
        this.mobContainer.setWorldAndPos(level, worldPosition);
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        this.mobContainer.load(compound);
        if(this.level != null){
            //onLoad();
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        this.mobContainer.save(compound);
        return compound;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
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
