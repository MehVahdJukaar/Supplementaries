package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.selene.fluids.ISoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//most complicated block ever
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;

public class JarBlockTile extends ItemDisplayTile implements TickableBlockEntity, IMobContainerProvider, ISoftFluidHolder {
    private final int CAPACITY = ServerConfigs.cached.JAR_CAPACITY;

    @Nonnull
    public MobContainer mobContainer;
    public SoftFluidHolder fluidHolder;

    public JarBlockTile() {
        super(ModRegistry.JAR_TILE.get());
        this.fluidHolder = new SoftFluidHolder(CAPACITY);
        AbstractMobContainerItem item = ((AbstractMobContainerItem) ModRegistry.JAR_ITEM.get());
        this.mobContainer = new MobContainer(item.getMobContainerWidth(), item.getMobContainerHeight(), this.level, this.worldPosition);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.mobContainer.setWorldAndPos(level, worldPosition);
    }

    @Override
    public double getViewDistance() {
        return 80;
    }

    @Override
    public void updateTileOnInventoryChanged() {
        this.level.updateNeighborsAt(worldPosition, this.getBlockState().getBlock());
        int light = this.fluidHolder.getFluid().getLuminosity();
        if (light != this.getBlockState().getValue(BlockProperties.LIGHT_LEVEL_0_15)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BlockProperties.LIGHT_LEVEL_0_15, light), 2);
        }
    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, InteractionHand hand) {

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack displayedStack = this.getDisplayedItem();

        //interact with fluid holder
        if (canInteractWithFluidHolder() && this.fluidHolder.interactWithPlayer(player, hand, level, worldPosition)) {
            return true;
        }
        //empty hand: eat food

        // can I insert this item? For cookies and fish buckets
        else if (this.mobContainer.isEmpty() && this.canPlaceItem(0, handStack)) {
            this.handleAddItem(handStack, player, hand);
            return true;
        }
        //fish buckets
        else if (this.isEmpty() && this.fluidHolder.isEmpty() && this.mobContainer.interactWithBucket(handStack, player.level, player.blockPosition(), player, hand)) {
            return true;
        }

        if (!player.isShiftKeyDown()) {
            //from drink
            if (ServerConfigs.cached.JAR_EAT) {
                if (this.fluidHolder.tryDrinkUpFluid(player, this.level)) return true;
                //cookies
                if (displayedStack.isEdible() && player.canEat(false) && !player.isCreative()) {
                    //eat cookies
                    player.eat(level, displayedStack);
                    return true;
                }
            }
        }
        //extract stuff
        return this.handleExtractItem(player, hand);
    }

    // removes item from te. only 1 increment
    public ItemStack extractItem() {
        ItemStack myStack = this.getDisplayedItem();
        if (myStack.getCount() > 0) {
            return myStack.split(1);
        }
        return ItemStack.EMPTY;
    }

    // removes item from te and gives it to player
    public boolean handleExtractItem(Player player, InteractionHand hand) {
        if (this.getDisplayedItem().getItem() instanceof FishBucketItem) {
            if (player.getItemInHand(hand).getItem() != Items.BUCKET) return false;
            this.level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (!player.getItemInHand(hand).isEmpty()) return false;
        ItemStack extracted = this.extractItem();
        if (!extracted.isEmpty()) {
            Utils.swapItem(player, hand, extracted);
            return true;
        }
        return false;
    }

    // adds item to te, removes from player
    public void handleAddItem(ItemStack stack, @Nullable Player player, InteractionHand handIn) {
        ItemStack handStack = stack.copy();
        handStack.setCount(1);
        Item item = handStack.getItem();

        this.addItem(handStack);

        if (player != null) {
            ItemStack returnStack = ItemStack.EMPTY;
            //TODO: cookie sounds
            player.awardStat(Stats.ITEM_USED.get(item));
            // shrink stack and replace bottle /bucket with empty ones
            if (!player.isCreative()) {
                Utils.swapItem(player, handIn, returnStack);
            }
        }
    }

    public void addItem(ItemStack itemstack) {
        if (this.isEmpty()) {
            NonNullList<ItemStack> stacks = NonNullList.withSize(1, itemstack);
            this.setItems(stacks);
        } else {
            this.getDisplayedItem().grow(Math.min(1, this.getMaxStackSize() - this.getDisplayedItem().getCount()));
        }
    }

    public void resetHolders() {
        this.fluidHolder.clear();
        this.mobContainer = null;
        this.setDisplayedItem(ItemStack.EMPTY);
    }

    public boolean isPonyJar() {
        //hahaha, funy pony jar meme
        if (this.hasCustomName()) {
            Component c = this.getCustomName();
            return (c != null && c.getString().contains("cum"));
        }
        return false;
    }

    //can this item be added?
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (this.fluidHolder.isEmpty() && this.mobContainer.isEmpty()) {
            Item i = stack.getItem();
            if (!this.isFull()) {
                //might add other accepted items here
                if (CommonUtil.isCookie(i)) {
                    return this.isEmpty() || i == this.getDisplayedItem().getItem();
                }
            }
        }
        return false;
    }



    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        this.fluidHolder.load(compound);
        this.mobContainer.load(compound);
        if(this.level != null){
            //onLoad();
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        //stacks are done by itemDisplayTile
        super.save(compound);
        this.fluidHolder.save(compound);
        this.mobContainer.save(compound);
        return compound;
    }

    public boolean hasContent() {
        return !(this.isEmpty() && this.mobContainer.isEmpty() && this.fluidHolder.isEmpty());
    }

    public boolean isFull() {
        return this.getDisplayedItem().getCount() >= this.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize() {
        return this.CAPACITY;
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.jar");
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        //can only insert cookies
        return CommonUtil.isCookie(stack.getItem()) && (this.isEmpty() || stack.getItem() == this.getDisplayedItem().getItem());
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public MobContainer getMobContainer() {
        return this.mobContainer;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ClockBlock.FACING);
    }

    @Override
    public void tick() {
        this.mobContainer.tick();
    }

    @Override
    public SoftFluidHolder getSoftFluidHolder() {
        return this.fluidHolder;
    }

    @Override
    public boolean canInteractWithFluidHolder() {
        return this.isEmpty() && (this.mobContainer.isEmpty() || isPonyJar());
    }
}