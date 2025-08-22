package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.LunchBoxBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.VariableSizeContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.common.items.components.LunchBaskedContent;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LunchBoxBlockTile extends OpeneableContainerBlockEntity {
    @Nullable
    private DyedItemColor dyeColor = null;

    public LunchBoxBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.LUNCH_BASKET_TILE.get(), pos, state, 27);
    }

    public static int getUnlockedSlots() {
        return CommonConfigs.Tools.LUNCH_BOX_SLOTS.get();
    }

    @Override
    public int getContainerSize() {
        return getUnlockedSlots();
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("gui.supplementaries.lunch_basket");
    }

    @Override
    protected void playOpenSound(BlockState state) {
        double d0 = (double) this.worldPosition.getX() + 0.5D;
        double d1 = (double) this.worldPosition.getY() + 1;
        double d2 = (double) this.worldPosition.getZ() + 0.5D;

        this.level.playSound(null, d0, d1, d2, ModSounds.LUNCH_BASKET_OPEN.get(), SoundSource.BLOCKS, 1,
                this.level.random.nextFloat() * 0.1F + 0.95F);
    }

    @Override
    protected void playCloseSound(BlockState state) {
        double d0 = (double) this.worldPosition.getX() + 0.5D;
        double d1 = (double) this.worldPosition.getY() + 1;
        double d2 = (double) this.worldPosition.getZ() + 0.5D;
        this.level.playSound(null, d0, d1, d2, ModSounds.LUNCH_BASKET_CLOSE.get(), SoundSource.BLOCKS, 1,
                this.level.random.nextFloat() * 0.1F + 0.8F);
    }

    @Override
    protected void updateBlockState(BlockState state, boolean open) {
        this.level.setBlock(this.getBlockPos(), state.setValue(LunchBoxBlock.OPEN, open), 3);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new VariableSizeContainerMenu(ModMenuTypes.LUNCH_BASKET.get(),
                id, player, this, getContainerSize());
    }

    public boolean isSlotUnlocked(int ind) {
        return ind < getContainerSize();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isSlotUnlocked(index) && LunchBoxItem.canAcceptItem(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return isSlotUnlocked(index);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (dyeColor != null) {
            var t = DyedItemColor.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), dyeColor)
                    .getOrThrow();
            tag.put("Color", t);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        var t = tag.get("Color");
        if (t != null) {
            dyeColor = DyedItemColor.CODEC.decode(registries.createSerializationContext(NbtOps.INSTANCE), t)
                    .getOrThrow().getFirst();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        var content = componentInput.get(ModComponents.LUNCH_BASKET_CONTENT.get());
        if (content != null) {
            for (int i = 0; i < getContainerSize(); i++) {
                this.setItem(i, content.getStackInSlot(i));
            }
        }
        this.dyeColor = componentInput.get(DataComponents.DYED_COLOR);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        var content = LunchBaskedContent.empty(getContainerSize()).toMutable();
        for (int i = 0; i < getContainerSize(); i++) {
            content.setStackInSlot(i, this.getItem(i));
        }
        components.set(ModComponents.LUNCH_BASKET_CONTENT.get(), content.toImmutable());
        if (this.dyeColor != null) components.set(DataComponents.DYED_COLOR, this.dyeColor);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Items");
    }

    @Nullable
    public DyedItemColor getDyedColor() {
        return dyeColor;
    }

    public void setDyedColor(DyedItemColor o) {
        this.dyeColor = o;
    }
}
