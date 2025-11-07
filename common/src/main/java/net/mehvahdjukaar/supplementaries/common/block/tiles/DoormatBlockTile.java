package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOnePlayerInteractable;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.supplementaries.client.screens.DoormatScreen;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DoormatBlockTile extends ItemDisplayTile implements ITextHolderProvider, IOnePlayerInteractable, IScreenProvider {
    public static final int MAX_LINES = 3;

    public final TextHolder textHolder;
    @Nullable
    private UUID playerWhoMayEdit;
    private boolean isWaxed = false;

    public DoormatBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.DOORMAT_TILE.get(), pos, state);
        this.textHolder = new TextHolder(MAX_LINES, 75);
    }

    @Override
    public TextHolder getTextHolder(int i) {
        return this.textHolder;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.textHolder.load(tag, registries, this.getBlockPos());
        if (tag.contains("Waxed")) {
            this.isWaxed = tag.getBoolean("Waxed");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.textHolder.save(tag, registries);
        if (isWaxed) tag.putBoolean("Waxed", isWaxed);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.supplementaries.doormat");
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(DoormatBlock.FACING);
    }

    @Override
    public void openScreen(Level level, Player player, Direction direction) {
        DoormatScreen.open(this);
    }

    @Override
    public SoundEvent getAddItemSound() {
        return SoundEvents.BRUSH_GENERIC;
    }

    @Override
    public void setWaxed(boolean waxed) {
        isWaxed = waxed;
    }

    @Override
    public boolean isWaxed() {
        return isWaxed;
    }

    @Override
    public void setPlayerWhoMayEdit(@Nullable UUID playerWhoMayEdit) {
        this.playerWhoMayEdit = playerWhoMayEdit;
    }

    @Override
    public UUID getPlayerWhoMayEdit() {
        return playerWhoMayEdit;
    }


    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}