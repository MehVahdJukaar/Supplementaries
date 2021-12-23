package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.client.gui.DoormatGui;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DoormatBlockTile extends ItemDisplayTile implements ITextHolderProvider {
    public static final int MAX_LINES = 3;

    public final TextHolder textHolder;

    public DoormatBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.DOORMAT_TILE.get(),pos, state);
        this.textHolder = new TextHolder(MAX_LINES);
    }

    public TextHolder getTextHolder(){return this.textHolder;}

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.textHolder.read(compound);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.textHolder.write(tag);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.doormat");
    }

    public Direction getDirection(){
        return this.getBlockState().getValue(DoormatBlock.FACING);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
        DoormatGui.open(this);
    }
}