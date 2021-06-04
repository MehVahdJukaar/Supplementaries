package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
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
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        this.mobHolder = new MobHolder(this.level,this.worldPosition);
        this.fluidHolder = new SoftFluidHolder(CAPACITY);
    }

    public MobHolder getMobHolder(){return this.mobHolder;}

    @Override
    public double getViewDistance() {
        return 80;
    }

    @Override
    public void onLoad() {
        this.mobHolder.setWorldAndPos(this.level, this.worldPosition);
        this.fluidHolder.setWorldAndPos(this.level, this.worldPosition);
    }

    // hijacking this method to work with hoppers
    @Override
    public void setChanged() {
        if(this.level==null)return;
        //TODO: only call after you finished updating your tile so others can react properly (faucets)
        this.level.updateNeighborsAt(worldPosition,this.getBlockState().getBlock());
        int light = this.fluidHolder.getFluid().getLuminosity();
        if(light!=this.getBlockState().getValue(BlockProperties.LIGHT_LEVEL_0_15)){
            this.level.setBlock(this.worldPosition,this.getBlockState().setValue(BlockProperties.LIGHT_LEVEL_0_15,light),2);
        }
        super.setChanged();
    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(PlayerEntity player, Hand hand) {

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack displayedStack = this.getDisplayedItem();

        //interact with fluid holder
        if (this.isEmpty() && (this.mobHolder.isEmpty()||isPonyJar()) && this.fluidHolder.interactWithPlayer(player, hand)) {
            return true;
        }
        //empty hand: eat food

        // can I insert this item? For cookies and fish buckets
        else if (this.mobHolder.isEmpty() && this.canPlaceItem(0, handStack)) {
            this.handleAddItem(handStack, player, hand);
            return true;
        }
        //fish buckets
        else if(this.isEmpty()&&this.fluidHolder.isEmpty()&&this.mobHolder.interactWithBucketItem(handStack, player, hand)){
            return true;
        }

        if(!player.isShiftKeyDown()) {
            //from drink
            if(ServerConfigs.cached.JAR_EAT) {
                if (this.fluidHolder.isFood()&&this.fluidHolder.drinkUpFluid(player, this.level, hand)) return true;
                //cookies
                if (displayedStack.isEdible() && player.canEat(false)) {
                    //eat cookies
                    /*
                    if (this.level.isClientSide) return true;
                    Food food = displayedStack.getItem().getFoodProperties();
                    player.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
                    this.extractItem();
                    player.playNotifySound(SoundEvents.GENERIC_EAT, SoundCategory.PLAYERS, 1, 1);
                    return true;*/
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
        if (myStack.getCount() > 0 ) {
            return myStack.split(1);
        }
        return ItemStack.EMPTY;
    }

    // removes item from te and gives it to player
    public boolean handleExtractItem(PlayerEntity player, Hand hand){
        if(this.getDisplayedItem().getItem()instanceof FishBucketItem){
            if(player.getItemInHand(hand).getItem()!=Items.BUCKET)return false;
            this.level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        else if(!player.getItemInHand(hand).isEmpty())return false;
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
            player.awardStat(Stats.ITEM_USED.get(item));
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
            this.getDisplayedItem().grow(Math.min(1, this.getMaxStackSize() - this.getDisplayedItem().getCount()));
        }
    }

    public void resetHolders(){
        this.fluidHolder.clear();
        this.mobHolder.clear();
        this.setDisplayedItem(ItemStack.EMPTY);
    }

    private boolean isPonyJar(){
        //hahaha, funy pony jar meme
        if(this.hasCustomName()){
            ITextComponent c = this.getCustomName();
            return (c!=null && c.getString().contains("cum"));
        }
        return false;
    }

    //can this item be added?
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
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
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        //todo: remove in future
        if(!(compound.contains("LiquidHolder") && this.convertOldJars(compound))) {
            this.fluidHolder.read(compound);
        }
        this.mobHolder.read(compound);

        this.onLoad();
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        //stacks are done by itemDisplayTile
        super.save(compound);
        this.mobHolder.write(compound);
        this.fluidHolder.write(compound);
        return compound;
    }

    public boolean hasContent(){
        return !(this.isEmpty()&&this.mobHolder.isEmpty()&&this.fluidHolder.isEmpty());
    }

    public boolean isFull(){
        return this.getDisplayedItem().getCount()>=this.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize() {
        return this.CAPACITY;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.jar");
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        //can only insert cookies
        return CommonUtil.isCookie(stack.getItem())&&(this.isEmpty()||stack.getItem()==this.getDisplayedItem().getItem());
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ClockBlock.FACING);
    }

    @Override
    public void tick() {
        this.mobHolder.tick();
    }
}