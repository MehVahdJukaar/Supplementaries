package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.block.util.IMobHolder;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidHolder;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class JarBlockTile extends ItemDisplayTile implements ITickableTileEntity, IMobHolder {
    private final int CAPACITY = ServerConfigs.cached.JAR_CAPACITY;

    public MobHolder mobHolder;
    public SoftFluidHolder fluidHolder;

    public JarBlockTile() {
        super(Registry.JAR_TILE.get());
        this.mobHolder = new MobHolder(this.world,this.pos);
        this.fluidHolder = new SoftFluidHolder(CAPACITY);
    }

    public MobHolder getMobHolder(){return this.mobHolder;}

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public void onLoad() {
        this.mobHolder.setWorldAndPos(this.world, this.pos);
        this.fluidHolder.setWorldAndPos(this.world, this.pos);
    }

    // hijacking this method to work with hoppers
    @Override
    public void markDirty() {
        //TODO: only call after you finished updating your tile so others can react properly (faucets)
        this.world.notifyNeighborsOfStateChange(pos,this.getBlockState().getBlock());
        super.markDirty();
    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(PlayerEntity player, Hand hand) {

        ItemStack handStack = player.getHeldItem(hand);
        ItemStack displayedStack = this.getDisplayedItem();

        //interact with fluid holder
        if (this.isEmpty() && this.mobHolder.isEmpty() &&this.fluidHolder.interactWithPlayer(player, hand)) {
            return true;
        }
        //empty hand: eat food

        // can I insert this item? For cookies and fish buckets
        else if (this.mobHolder.isEmpty() && this.isItemValidForSlot(0, handStack)) {
            this.handleAddItem(handStack, player, hand);
            return true;
        }
        //fish buckets
        else if(this.isEmpty()&&this.fluidHolder.isEmpty()&&this.mobHolder.interactWithBucketItem(handStack, player, hand)){
            return true;
        }

        if(!player.isSneaking()) {
            boolean canDrinkFromJar = ServerConfigs.cached.JAR_EAT;
            //from drink
            if (canDrinkFromJar && this.fluidHolder.drinkUpFluid(player, this.world, hand)) return true;
            //cookies
            if (displayedStack.isFood()) {
                //eat cookies
                if (player.canEat(false) && canDrinkFromJar) {
                    if (this.world.isRemote) return true;
                    Food food = displayedStack.getItem().getFood();
                    player.getFoodStats().addStats(food.getHealing(), food.getSaturation());
                    this.extractItem();
                    player.playSound(SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 1, 1);
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
        if (myStack.getCount() > 0 ) {
            return myStack.split(1);
        }
        return ItemStack.EMPTY;
    }

    // removes item from te and gives it to player
    public boolean handleExtractItem(PlayerEntity player, Hand hand){
        if(this.getDisplayedItem().getItem()instanceof FishBucketItem){
            if(player.getHeldItem(hand).getItem()!=Items.BUCKET)return false;
            this.world.playSound(null, player.getPosition(), SoundEvents.ITEM_BUCKET_FILL_FISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        else if(!player.getHeldItem(hand).isEmpty())return false;
        ItemStack extracted = this.extractItem();
        if(!extracted.isEmpty()) {
            CommonUtil.swapItem(player,hand,extracted);
            return true;
        }
        return false;
    }

    // adds item to te, removes from player
    public void handleAddItem(ItemStack stack, @Nullable PlayerEntity player, Hand handIn) {
        ItemStack handStack = stack.copy();
        handStack.setCount(1);
        Item item = handStack.getItem();

        this.addItem(handStack);

        if(player!=null) {
            ItemStack returnStack = ItemStack.EMPTY;
            //TODO: cookie sounds
            player.addStat(Stats.ITEM_USED.get(item));
            // shrink stack and replace bottle /bucket with empty ones
            if (!player.isCreative()) {
                CommonUtil.swapItem(player, handIn, returnStack);
            }
        }
    }

    public void addItem(ItemStack itemstack) {
        if (this.isEmpty()) {
            NonNullList<ItemStack> stacks = NonNullList.withSize(1, itemstack);
            this.setItems(stacks);
        } else {
            this.getDisplayedItem().grow(Math.min(1, this.getInventoryStackLimit() - this.getDisplayedItem().getCount()));
        }
    }

    public void resetHolders(){
        this.fluidHolder.clear();
        this.mobHolder.clear();
        this.setDisplayedItem(ItemStack.EMPTY);
    }

    //can this item be added?
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if(this.fluidHolder.isEmpty() && this.mobHolder.isEmpty()) {
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

    public boolean convertOldJars(CompoundNBT compound){
        if(compound==null)return false;
        NonNullList<ItemStack> oldStacks = NonNullList.withSize(1, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, oldStacks);
        ItemStack oldStack = oldStacks.get(0);
        if(!oldStacks.isEmpty()&&!oldStack.isEmpty()) {
            this.fluidHolder.clear();
            if(!this.fluidHolder.interactWithItem(oldStack).isEmpty()){
                if(compound.contains("LiquidHolder")) {
                    this.fluidHolder.setCount((int) (compound.getCompound("LiquidHolder").getFloat("Level") * 16));
                }
                else{
                    this.fluidHolder.lossyAdd(oldStack.getCount());
                }
                this.stacks = NonNullList.withSize(1, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        //todo: remove in future
        if(!(compound.contains("LiquidHolder") && this.convertOldJars(compound))) {
            this.fluidHolder.read(compound);
        }
        this.mobHolder.read(compound);

        this.onLoad();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        //stacks are done by itemDisplayTile
        super.write(compound);
        this.mobHolder.write(compound);
        this.fluidHolder.write(compound);
        return compound;
    }

    public boolean hasContent(){
        return !(this.isEmpty()&&this.mobHolder.isEmpty()&&this.fluidHolder.isEmpty());
    }

    public boolean isFull(){
        return this.getDisplayedItem().getCount()>=this.getInventoryStackLimit();
    }

    @Override
    public int getInventoryStackLimit() {
        return this.CAPACITY;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.jar");
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
        //can only insert cookies
        return CommonUtil.isCookie(stack.getItem())&&(this.isEmpty()||stack.getItem()==this.getDisplayedItem().getItem());
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }

    @Override
    public void tick() {
        this.mobHolder.tick();
    }
}