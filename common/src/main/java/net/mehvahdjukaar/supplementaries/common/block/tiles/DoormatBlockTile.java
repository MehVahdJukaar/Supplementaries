package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.DoormatBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.client.screens.DoormatGui;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DoormatBlockTile extends ItemDisplayTile implements ITextHolderProvider {
    public static final int MAX_LINES = 3;

    public final TextHolder textHolder;

    public DoormatBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.DOORMAT_TILE.get(),pos, state);
        this.textHolder = new TextHolder(MAX_LINES, 75);
    }

    @Override
    public TextHolder getTextHolder(){return this.textHolder;}

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.textHolder.load(compound);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.textHolder.save(tag);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.supplementaries.doormat");
    }

    public Direction getDirection(){
        return this.getBlockState().getValue(DoormatBlock.FACING);
    }

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
        DoormatGui.open(this);
    }
}