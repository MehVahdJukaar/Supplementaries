package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarContentType;
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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
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

public class JarBlockTile extends LockableLootTileEntity implements ISidedInventory {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    public int color = 0xffffff;
    public float liquidLevel = 0;
    public JarContentType liquidType = JarContentType.EMPTY;
    public JarBlockTile() {
        super(Registry.JAR_TILE);
    }


    // hijacking this method to work with hoppers
    @Override
    public void markDirty() {
        // this.updateServerAndClient();
        this.updateTile();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.markDirty();
    }

    public void updateTile() {

        ItemStack stack = this.getStackInSlot(0);

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
        else if(this.liquidType == JarContentType.POTION){
            this.color = PotionUtils.getColor(stack);
        }
        else if (this.liquidType.applyColor){
            this.color = this.liquidType.color;
        }
        else{
            this.color = 0xFFFFFF;
        }

        //lava light
        if(!this.world.isRemote && this.liquidType.isLava()){
            BlockState bs = this.world.getBlockState(this.pos);
            if (!bs.get(JarBlock.HAS_LAVA)) {
                this.world.setBlockState(this.pos, bs.with(JarBlock.HAS_LAVA, true), 4|16);
            }

        }

    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(PlayerEntity player, Hand hand) {
        ItemStack handstack = player.getHeldItem(hand);
        Item handitem = handstack.getItem();
        boolean isbucket = handitem == Items.BUCKET;
        boolean isbottle = handitem == Items.GLASS_BOTTLE;
        boolean isbowl = handitem == Items.BOWL;
        boolean isempty = handstack.isEmpty();
        // eat cookies
        if (isempty && this.liquidType == JarContentType.COOKIES) {
            boolean eat = false;
            if (player.canEat(false))
                eat = true;
            if (this.extractItem(1, handstack, player, hand, !eat)) {
                if (eat)
                    player.getFoodStats().addStats(2, 0.1F);
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
                if (this.extractItem(1, handstack, player, hand, true)) {
                    this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL,
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
                if (this.extractItem(4, handstack, player, hand, true)) {
                    this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), this.liquidType.getSound(),
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
                if (this.extractItem(2, handstack, player, hand, true)) {
                    this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                    player.addStat(Stats.ITEM_USED.get(Items.BOWL));
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    // removes item from te and gives it to player
    public boolean extractItem(int amount, ItemStack handstack, PlayerEntity player, Hand handIn, boolean givetoplayer) {
        amount = this.liquidType.isFish() ? 1 : amount;
        ItemStack mystack = this.getStackInSlot(0);
        int count = mystack.getCount();
        // do i have enough?
        if (count >= amount) {
            if (!player.isCreative() && givetoplayer) {
                ItemStack extracted = mystack.copy();
                extracted.setCount(1);
                // special case to convert water bottles into bucket
                if (this.liquidType == JarContentType.WATER && amount==4) {
                    extracted = new ItemStack(Items.WATER_BUCKET);
                }
                handstack.shrink(1);
                if (handstack.isEmpty()) {
                    player.setHeldItem(handIn, extracted);
                } else if (!player.inventory.addItemStackToInventory(extracted)) {
                    player.dropItem(extracted, false);
                }
                /*
                 * else if (player instanceof ServerPlayerEntity) {
                 * ((ServerPlayerEntity)player).sendContainerToPlayer(player.container); }
                 */
            }
            mystack.setCount(Math.max(0, count - amount));
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
        //update liquidType after adding item
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
                this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_EMPTY,
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

    }

    public void addItem(ItemStack itemstack, int amount) {
        if (this.isEmpty()) {
            itemstack.setCount(amount);
            NonNullList<ItemStack> stacks = NonNullList.withSize(1, itemstack);
            this.setItems(stacks);
        } else {
            this.getStackInSlot(0).grow(Math.min(amount, this.getInventoryStackLimit() - amount));
        }
    }

    public boolean isFull() {
        return this.getStackInSlot(0).getCount() >= this.getInventoryStackLimit();
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) return false;
        ItemStack currentstack = this.getStackInSlot(0);
        Item newitem = stack.getItem();
        Item currentitem = currentstack.getItem();
        // is it potion
        if (newitem instanceof PotionItem) {
            return (this.isEmpty() || ((PotionUtils.getPotionFromItem(stack) == PotionUtils.getPotionFromItem(currentstack)) && !this.isFull()));
        }
        // is it waterbucket (check it it has water bottle)
        else if (newitem == Items.WATER_BUCKET) {
            return (this.isEmpty() || (PotionUtils.getPotionFromItem(currentstack) == Potions.WATER && !this.isFull()));
        }
        // other items (stack to 12)
        else if (newitem instanceof ExperienceBottleItem || newitem instanceof HoneyBottleItem || newitem instanceof MilkBucketItem
                || newitem == Items.LAVA_BUCKET
                || newitem == Items.DRAGON_BREATH || newitem == Items.COOKIE) {
            return (this.isEmpty() || (currentitem == newitem && !this.isFull()));
        }
        // fish bucket (only 1 can stay in)
        else if (newitem instanceof FishBucketItem) {
            return this.isEmpty();
        }
        //stews, stack to 6
        else if (newitem == Items.MUSHROOM_STEW|| newitem == Items.RABBIT_STEW||
                newitem==Items.BEETROOT_SOUP){
            return (this.isEmpty() || (currentitem == newitem && !this.isFull()));
        }
        else if(newitem instanceof SuspiciousStewItem){
            return (currentstack.getOrCreateTag().equals(stack.getOrCreateTag()) || this.isEmpty())&&!this.isFull();
        }
        return false;
    }

    /*
     * public void loadFromNbt(CompoundNBT compound) { this.stacks =
     * NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY); if
     * (!this.checkLootAndRead(compound) && compound.contains("Items", 9)) {
     * ItemStackHelper.loadAllItems(compound, this.stacks);
     *
     * MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
     * mcserv.getPlayerList().sendMessage(new StringTextComponent("no"));
     *
     * if(compound.contains("fluidLevel")){ mcserv.getPlayerList().sendMessage(new
     * StringTextComponent("nwewo")); this.fluidLevel =
     * compound.getFloat("fluidLevel"); } } }
     */
    // save to itemstack
    public CompoundNBT saveToNbt(CompoundNBT compound) {
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks, false);
            if (this.liquidLevel != 0) {
                compound.putFloat("liquidLevel", this.liquidLevel);
                compound.putInt("liquidType", this.liquidType.ordinal());
                compound.putInt("liquidColor", this.liquidType.bucket ? this.liquidType.color : this.color);
            }
        }
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (!this.checkLootAndRead(compound)) {
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        }
        ItemStackHelper.loadAllItems(compound, this.stacks);
        this.liquidLevel = compound.getFloat("liquid_level");
        this.color = compound.getInt("liquid_color");
        this.liquidType = JarContentType.values()[compound.getInt("liquid_type")];
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

    @Override
    public ITextComponent getDefaultName() {
        return new StringTextComponent("jar");
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
        //return this.isItemValidForSlot(index, stack) && (this.liquidType == JarContentType.COOKIES || this.liquidType == JarContentType.EMPTY);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return this.liquidType == JarContentType.COOKIES;
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

}