package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.PulleyBlockContainerMenu;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;


public class PulleyBlockTile extends ItemDisplayTile {

    public PulleyBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PULLEY_BLOCK_TILE.get(), pos, state);
    }

    //no need since it doesn't display stuff
    @Override
    public boolean needsToUpdateClientWhenChanged() {
        return false;
    }

    public void updateTileOnInventoryChanged() {
        Winding type = getContentType(this.getDisplayedItem().getItem());
        BlockState state = this.getBlockState();
        if (state.getValue(PulleyBlock.TYPE) != type) {
            level.setBlockAndUpdate(this.worldPosition, state.setValue(PulleyBlock.TYPE, type));
        }
    }

    public static Winding getContentType(Item item) {
        Winding type = Winding.NONE;
        if (item instanceof BlockItem bi && bi.getBlock() instanceof ChainBlock || item.builtInRegistryHolder().is(ModTags.CHAINS))
            type = Winding.CHAIN;
        else if (item.builtInRegistryHolder().is(ModTags.ROPES)) type = Winding.ROPE;
        return type;
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.pulley_block");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new PulleyBlockContainerMenu(id, player, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return (getContentType(stack.getItem()) != Winding.NONE);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }


    public boolean handleRotation(Rotation rot, BlockPos pos) {
        if (rot == Rotation.CLOCKWISE_90) return this.pullUp(pos, this.level, 1);
        else return this.pullDown(pos, this.level, 1);
    }

    public boolean pullUp(BlockPos pos, LevelAccessor world, int rot) {

        if (!(world instanceof Level)) return false;
        ItemStack stack = this.getDisplayedItem();
        boolean addNewItem = false;
        if (stack.isEmpty()) {
            Item i = world.getBlockState(pos.below()).getBlock().asItem();
            if (getContentType(i) == Winding.NONE) return false;
            stack = new ItemStack(i);
            addNewItem = true;
        }
        if (stack.getCount() + rot > stack.getMaxStackSize() || !(stack.getItem() instanceof BlockItem)) return false;
        Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
        boolean success = RopeBlock.removeRope(pos.below(), (Level) world, ropeBlock);
        if (success) {
            SoundType soundtype = ropeBlock.defaultBlockState().getSoundType(world, pos, null);
            world.playSound(null, pos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            if (addNewItem) this.setDisplayedItem(stack);
            else stack.grow(1);
            this.setChanged();
        }
        return success;
    }

    public boolean pullDown(BlockPos pos, LevelAccessor world, int rot) {

        if (!(world instanceof Level)) return false;
        ItemStack stack = this.getDisplayedItem();
        if (stack.getCount() < rot || !(stack.getItem() instanceof BlockItem)) return false;
        Block ropeBlock = ((BlockItem) stack.getItem()).getBlock();
        boolean success = RopeBlock.addRope(pos.below(), (Level) world, null, InteractionHand.MAIN_HAND, ropeBlock);
        if (success) {
            SoundType soundtype = ropeBlock.defaultBlockState().getSoundType(world, pos, null);
            world.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            stack.shrink(1);
            this.setChanged();
        }
        return success;
    }

}