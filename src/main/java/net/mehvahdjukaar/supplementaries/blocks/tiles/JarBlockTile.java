package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarLiquidType;
import net.mehvahdjukaar.supplementaries.common.IMobHolder;
import net.mehvahdjukaar.supplementaries.common.LiquidHolder;
import net.mehvahdjukaar.supplementaries.common.MobHolder;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class JarBlockTile extends LockableLootTileEntity implements ISidedInventory, ITickableTileEntity, IMobHolder {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    public int color = 0xffffff;
    public float liquidLevel = 0;
    public JarLiquidType liquidType = JarLiquidType.EMPTY;

    public MobHolder mobHolder;
    public LiquidHolder liquidHolder;

    public JarBlockTile() {
        super(Registry.JAR_TILE);
        this.mobHolder = new MobHolder(this.world,this.pos);
        this.liquidHolder = new LiquidHolder(this.world,this.pos);
    }

    public MobHolder getMobHolder(){return this.mobHolder;}

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public void onLoad() {
        this.mobHolder.setWorldAndPos(this.world,this.pos);
        this.liquidHolder.setWorldAndPos(this.world,this.pos);
    }

    // hijacking this method to work with hoppers
    @Override
    public void markDirty() {
        // this.updateServerAndClient();
        this.updateTile();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        this.world.notifyNeighborsOfStateChange(pos,this.getBlockState().getBlock());
        super.markDirty();
    }

    //called by markdirty. server side. client will receive updated values via update packet and read()
    public void updateTile() {
        ItemStack stack = this.getStackInSlot(0);

        this.liquidHolder.updateLiquid(stack);

        this.liquidType = CommonUtil.getJarContentTypeFromItem(stack);
        //level
        if(this.liquidType.isFish()){
            this.liquidLevel = 0.625f;
        }
        else{
            this.liquidLevel = ((float) this.getStackInSlot(0).getCount()/ (float)ServerConfigs.cached.JAR_CAPACITY)*0.75f;
        }
        //color
        if(this.liquidType.isWater()){
            this.color=-1;//let client get biome color on next rendering. ugly i know but that class is client side
        }
        else if(this.liquidType == JarLiquidType.POTION){
            this.color = PotionUtils.getColor(stack);
        }
        else{
            this.color = this.liquidType.color;
        }
        //lava light
        //dis

    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(PlayerEntity player, Hand hand) {
        ItemStack handstack = player.getHeldItem(hand);
        Item handitem = handstack.getItem();
        boolean isbucket = handitem == Items.BUCKET;
        boolean isbottle = handitem == Items.GLASS_BOTTLE;
        boolean isbowl = handitem == Items.BOWL;
        boolean isempty = handstack.isEmpty();
        // eat food
        boolean candrinkfromjar = ServerConfigs.cached.JAR_EAT;
        if(isempty && !player.isSneaking()) {
            ItemStack stack = this.getStackInSlot(0);
            Item it = stack.getItem();
            if(stack.isFood()){
                //eat foods
                if (player.canEat(false) && candrinkfromjar) {
                    if(this.world.isRemote)return true;
                    Food food = it.getFood();
                    int div = CommonUtil.getLiquidCountFromItem(it);
                    player.getFoodStats().addStats(food.getHealing()/div, food.getSaturation()/(float)div);
                    //add stew effects
                    if(it instanceof  SuspiciousStewItem){
                        //stew code
                        CompoundNBT compoundnbt = stack.getTag();
                        if (compoundnbt != null && compoundnbt.contains("Effects", 9)) {
                            ListNBT listnbt = compoundnbt.getList("Effects", 10);
                            for(int i = 0; i < listnbt.size(); ++i) {
                                int j = 160;
                                CompoundNBT compoundnbt1 = listnbt.getCompound(i);
                                if (compoundnbt1.contains("EffectDuration", 3))
                                    j = compoundnbt1.getInt("EffectDuration")/div;
                                Effect effect = Effect.get(compoundnbt1.getByte("EffectId"));
                                if (effect != null) {
                                    player.addPotionEffect(new EffectInstance(effect, j));
                                }
                            }
                        }
                    }
                    this.extractItem(1);
                    this.world.playSound(player,pos, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS,1,1);
                    return true;
                }
                //extract cookies with empty hand
                else if(this.liquidType == JarLiquidType.COOKIES){
                    this.handleExtractItem(1, handstack, player, hand);
                    return true;
                }
            }
            //drink potions
            else if(it instanceof PotionItem && candrinkfromjar){
                if(this.world.isRemote)return true;
                //potion code
                for(EffectInstance effectinstance : PotionUtils.getEffectsFromStack(stack)) {
                    if (effectinstance.getPotion().isInstant()) {
                        effectinstance.getPotion().affectEntity(player, player, player, effectinstance.getAmplifier(), 1.0D);
                    } else {
                        player.addPotionEffect(new EffectInstance(effectinstance));
                    }
                }
                this.extractItem(1);
                this.world.playSound(null,pos, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS,1,1);
                return true;

            }
        }
        // can I insert this item?
        else if (this.isItemValidForSlot(0, handstack)) {
            this.handleAddItem(handstack, player, hand);
            return true;
        }
        // is hand item bottle?
        else if (isbottle) {
            // can content be extracted with bottle
            if (this.liquidType.bottle) {
                // if extraction successful
                if (this.handleExtractItem(1, handstack, player, hand)) {
                    this.world.playSound(player, player.getPosition(), SoundEvents.ITEM_BOTTLE_FILL,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                    player.addStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
                    return true;
                }
            }
            return false;
        }
        // is hand item bucket?
        else if (isbucket) {
            // can content be extracted with bucket
            if (this.liquidType.bucket) {
                // if extraction successful
                if (this.handleExtractItem(3, handstack, player, hand)) {
                        this.world.playSound(player, player.getPosition(), this.liquidType.getSound(),
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                    player.addStat(Stats.ITEM_USED.get(Items.BUCKET));
                    return true;
                }
            }
            return false;
        }
        else if (isbowl) {
            // can content be extracted with bowl
            if (this.liquidType.bowl) {
                // if extraction successful
                if (this.handleExtractItem(2, handstack, player, hand)) {
                    this.world.playSound(player, player.getPosition(), SoundEvents.ITEM_BOTTLE_FILL,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                    player.addStat(Stats.ITEM_USED.get(Items.BOWL));
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    // removes item from te
    public ItemStack extractItem(int amount) {
        amount = this.liquidType.isFish() ? 1 : amount;
        ItemStack mystack = this.getStackInSlot(0);
        int count = mystack.getCount();
        // do i have enough?
        if (count >= amount) {
            //case for cookies
            ItemStack extracted = mystack.copy();
            extracted.setCount(1);
            // special case to convert water bottles into bucket
            if (this.liquidType == JarLiquidType.WATER && amount==3) {
                extracted = new ItemStack(Items.WATER_BUCKET);
            }
            mystack.setCount(Math.max(0, count - amount));
            return extracted;
        }
        return null;
    }

    // removes item from te and gives it to player
    public boolean handleExtractItem(int amount, ItemStack handstack, @Nullable PlayerEntity player, @Nullable Hand handIn){
        ItemStack extracted = this.extractItem(amount);
        if(extracted!=null) {
            player.setHeldItem(handIn, DrinkHelper.fill(handstack.copy(), player, extracted, true));
            return true;
        }
        return false;
    }


    // adds item to te, removes from player
    public void handleAddItem(ItemStack handstack, @Nullable PlayerEntity player, @Nullable Hand handIn) {
        ItemStack it = handstack.copy();
        Item i = it.getItem();

        //add item
        int count = CommonUtil.getLiquidCountFromItem(i);
        //convert water bucket to bottle
        boolean isWaterBucket = i == Items.WATER_BUCKET;
        if(isWaterBucket)
            it = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER);

        this.addItem(it, count);
        //update liquidType after adding item. this should' be here. too bad
        this.updateTile();


        // shrink stack and replace bottle /bucket with empty ones
        if (player != null && handIn != null) {
            if (!player.isCreative()) {
                handstack.shrink(1);
                ItemStack returnItem = new ItemStack(isWaterBucket? Items.BUCKET : this.liquidType.getReturnItem());
                if (handstack.isEmpty()) {
                    player.setHeldItem(handIn, returnItem);
                } else if (!player.inventory.addItemStackToInventory(returnItem)) {
                    player.dropItem(returnItem, false);
                }
            }
            if (this.liquidType.makesSound())
                this.world.playSound(player, player.getPosition(), SoundEvents.ITEM_BOTTLE_EMPTY,
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

    }

    public void addItem(ItemStack itemstack, int amount) {
        if (this.isEmpty()) {
            itemstack.setCount(amount);
            NonNullList<ItemStack> stacks = NonNullList.withSize(1, itemstack);
            this.setItems(stacks);
        } else {
            this.getStackInSlot(0).grow(Math.min(amount, this.getInventoryStackLimit() - this.getStackInSlot(0).getCount()));
        }
    }

    public boolean isFull() {
        return (this.liquidType.isFish() || this.getStackInSlot(0).getCount() >= this.getInventoryStackLimit());
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        //c*m jar easter egg XD. so funi XDD
        if(!this.mobHolder.isEmpty()){
            if(!this.hasCustomName())return false;
            else if(!this.getCustomName().toString().toLowerCase().contains("cum"))return false;
        }
        //bottom text

        //TODO rewrite this to use forge fluid system
        //convert water buckets
        if(stack.getItem() == Items.WATER_BUCKET)
            stack = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER);

        JarLiquidType lt = CommonUtil.getJarContentTypeFromItem(stack);
        if(!lt.isEmpty() && index == 0){
            ItemStack currentStack = this.getStackInSlot(0);
            if (this.isEmpty()) return true;
            else if(!this.isFull()&&this.liquidType==lt){
                return currentStack.getOrCreateTag().equals(stack.getOrCreateTag());
            }
        }
        return false;
    }

    //TODO: use write instead so you can pass stuff directly when usign blockEntityTag

    // save to itemstack
    public void saveToNbt(ItemStack stack) {
        //TODO: replace with write
        //liquid stuff
        CompoundNBT compound = new CompoundNBT();
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks, false);
            if (this.liquidLevel != 0) {
                compound.putFloat("liquidLevel", this.liquidLevel);
                compound.putInt("liquidType", this.liquidType.ordinal());
                compound.putInt("liquidColor", this.liquidType.bucket ? this.liquidType.color : this.color);
            }
        }
        this.mobHolder.write(compound);
        this.liquidHolder.write(compound);

        if (!compound.isEmpty())
            stack.setTagInfo("BlockEntityTag", compound);

    }

    //TODO: convert to liquid holder
    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (!this.checkLootAndRead(compound)) {
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        }
        ItemStackHelper.loadAllItems(compound, this.stacks);

        this.liquidLevel = compound.getFloat("liquid_level");
        this.color = compound.getInt("liquid_color");
        this.liquidType = JarLiquidType.values()[compound.getInt("liquid_type")];

        this.mobHolder.read(compound);
        this.liquidHolder.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks);
        }
        compound.putInt("liquid_color", this.color);
        compound.putFloat("liquid_level", this.liquidLevel);
        compound.putInt("liquid_type", this.liquidType.ordinal());

        this.mobHolder.write(compound);
        this.liquidHolder.write(compound);

        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public int getSizeInventory() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }


    public boolean hasContent(){
        return !(this.isEmpty()&&this.mobHolder.isEmpty());
    }

    @Override
    public int getInventoryStackLimit() {
        return ServerConfigs.cached.JAR_CAPACITY;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return ChestContainer.createGeneric9X3(id, player, this);
    }

    @Override
    public ITextComponent getDefaultName() {
        return new StringTextComponent("Jar");
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Jar");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getSizeInventory()).toArray();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
        //can only insert cookies
        return stack.getItem() == Items.COOKIE;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return this.liquidType == JarLiquidType.COOKIES;
    }
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void remove() {
        super.remove();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }

    @OnlyIn(Dist.CLIENT)
    public int updateClientWaterColor(){
        this.color = BiomeColors.getWaterColor(this.world, this.pos);
        return this.color;
    }

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }

    @Override
    public void tick() {
        this.mobHolder.tick();
    }

}