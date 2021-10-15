package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolder;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class DoormatBlockTile extends ItemDisplayTile implements ITextHolder {
    public static final int MAXLINES = 3;

    public TextHolder textHolder;


    public DoormatBlockTile() {
        super(ModRegistry.DOORMAT_TILE.get());
        this.textHolder = new TextHolder(MAXLINES);
    }

    @Override
    public TextHolder getTextHolder(){return this.textHolder;}

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        this.textHolder.read(compound);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        this.textHolder.write(compound);
        return compound;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.doormat");
    }

    public Direction getDirection(){
        return this.getBlockState().getValue(DoormatBlock.FACING);
    }

    //TODO: optimize this two methods to send only what's needed
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

}