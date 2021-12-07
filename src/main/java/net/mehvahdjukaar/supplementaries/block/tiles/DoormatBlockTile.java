package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.client.gui.DoormatGui;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DoormatBlockTile extends ItemDisplayTile implements ITextHolderProvider {
    public static final int MAXLINES = 3;

    public TextHolder textHolder;


    public DoormatBlockTile() {
        super(ModRegistry.DOORMAT_TILE.get());
        this.textHolder = new TextHolder(MAXLINES);
    }

    @Override
    public TextHolder getTextHolder(){return this.textHolder;}

    @OnlyIn(Dist.CLIENT)
    public Screen getScreen() {
        return new DoormatGui(this);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.textHolder.read(compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        this.textHolder.write(compound);
        return compound;
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.doormat");
    }

    public Direction getDirection(){
        return this.getBlockState().getValue(DoormatBlock.FACING);
    }

    //TODO: optimize this two methods to send only what's needed
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

}