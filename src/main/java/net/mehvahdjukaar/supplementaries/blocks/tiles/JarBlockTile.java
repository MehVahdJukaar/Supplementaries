package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarLiquidType;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarMobType;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.RabbitEntity;
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
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
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
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

public class JarBlockTile extends LockableLootTileEntity implements ISidedInventory, ITickableTileEntity {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    public int color = 0xffffff;
    public float liquidLevel = 0;
    public JarLiquidType liquidType = JarLiquidType.EMPTY;

    //mob jar code
    public Entity mob = null;
    public CompoundNBT entityData = null;
    public UUID uuid;
    public boolean entityChanged = true;
    public float yOffset = 1;
    public float scale = 1;
    public float jumpY = 0;
    public float prevJumpY = 0;
    public float yVel = 0;
    private final Random rand = new Random();
    public JarMobType animationType = CommonUtil.JarMobType.DEFAULT;

    public JarBlockTile() {
        super(Registry.JAR_TILE);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
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
        if(!this.world.isRemote && this.liquidType.isLava()){
            BlockState bs = this.getBlockState();
            if (bs.get(JarBlock.LIGHT_LEVEL)!=15) {
                this.world.setBlockState(this.pos, bs.with(JarBlock.LIGHT_LEVEL, 15), 4|16);
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
            this.getStackInSlot(0).grow(Math.min(amount, this.getInventoryStackLimit() - amount));
        }
    }

    public boolean isFull() {
        return (this.liquidType.isFish() || this.getStackInSlot(0).getCount() >= this.getInventoryStackLimit());
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        //c*m jar easter egg XD
        if(!this.hasNoMob()){
            if(!this.hasCustomName())return false;
            else if(!this.getCustomName().toString().toLowerCase().contains("cum"))return false;
        }

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

    // save to itemstack
    public void saveToNbt(ItemStack stack) {
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
        if (!compound.isEmpty())
            stack.setTagInfo("BlockEntityTag", compound);
        //jar mob stuff
        if(this.mob==null||entityData==null)return;
        CompoundNBT cmp = new CompoundNBT();
        cmp.putFloat("Scale", this.scale);
        cmp.putFloat("YOffset", this.yOffset);
        cmp.putString("Name",this.mob.getName().getString());
        if(this.uuid!=null)
            cmp.putUniqueId("oldID",this.uuid);
        stack.setTagInfo("CachedJarMobValues", cmp);
        stack.setTagInfo("JarMob", entityData);
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
        this.liquidType = JarLiquidType.values()[compound.getInt("liquid_type")];
        //mob jar
        //TODO: reformat all nbts to be consistent
        if(compound.contains("jar_mob")){
            this.entityData = compound.getCompound("jar_mob");
            //this.updateMob();
            this.entityChanged = true;
        }
        this.scale = compound.getFloat("scale");
        this.yOffset = compound.getFloat("y_offset");
        this.animationType = JarMobType.values()[compound.getInt("animation_type")];
        if(compound.contains("uuid"))
            this.uuid = compound.getUniqueId("uuid");
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
        //mob jar
        if(this.entityData!=null)
            compound.put("jar_mob", this.entityData);
        compound.putFloat("scale",this.scale);
        compound.putFloat("y_offset",this.yOffset);
        compound.putInt("animation_type", this.animationType.ordinal());
        if(this.uuid!=null)
            compound.putUniqueId("uuid",this.uuid);
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
    public int getInventoryStackLimit() {
        return ServerConfigs.cached.JAR_CAPACITY;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return ChestContainer.createGeneric9X3(id, player, this);
    }

    @Override
    public ITextComponent getDefaultName() {
        return new StringTextComponent("jar");
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

    //mob jar code

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }

    public void tick() {
        if(this.entityChanged && this.entityData!=null)this.updateMob();
        if (!this.world.isRemote) return;
        //for client side animation
        if (this.mob != null) {
            this.mob.ticksExisted++;
            this.prevJumpY = this.jumpY;
            switch (this.animationType) {
                default:
                case DEFAULT:
                    break;
                case SLIME:
                case MAGMA_CUBE:
                    SlimeEntity slime = (SlimeEntity) this.mob;
                    slime.squishFactor += (slime.squishAmount - slime.squishFactor) * 0.5F;
                    slime.prevSquishFactor = slime.squishFactor;
                    //move
                    if (this.yVel != 0)
                        this.jumpY = Math.max(0, this.jumpY + this.yVel);
                    if (jumpY != 0) {
                        //decelerate
                        this.yVel = this.yVel - 0.010f;
                    }
                    //on ground
                    else {
                        if (this.yVel != 0) {
                            //land
                            this.yVel = 0;
                            slime.squishAmount = -0.5f;
                        }
                        if (this.rand.nextFloat() > 0.985) {
                            //jump
                            this.yVel = 0.08f;
                            slime.squishAmount = 1.0F;
                        }
                    }
                    slime.squishAmount *= 0.6F;
                    break;
                case VEX:
                    this.jumpY = 0.04f * MathHelper.sin(this.mob.ticksExisted / 10f) - 0.03f;
                    break;
                case ENDERMITE:
                    if (this.rand.nextFloat() > 0.7f) {
                        this.world.addParticle(ParticleTypes.PORTAL, this.pos.getX() + 0.5f, this.pos.getY() + 0.2f,
                                this.pos.getZ() + 0.5f, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
                    }
                    break;
                case PARROT:
                    ParrotEntity parrot = (ParrotEntity) this.mob;
                    parrot.livingTick();
                    parrot.setOnGround(false);
                    break;
                case PIXIE:
                    LivingEntity le = ((LivingEntity)this.mob);
                    le.livingTick();
                    le.lastTickPosY=this.pos.getY();
                    le.setPosition(le.getPosX(),this.pos.getY(),le.getPosZ());
                    break;
                case RABBIT:
                    RabbitEntity rabbit = (RabbitEntity) this.mob;
                    //move
                    if (this.yVel != 0)
                        this.jumpY = Math.max(0, this.jumpY + this.yVel);
                    if (jumpY != 0) {
                        //decelerate
                        this.yVel = this.yVel - 0.017f;
                    }
                    //on ground
                    else {
                        if (this.yVel != 0) {
                            //land
                            this.yVel = 0;
                        }
                        if (this.rand.nextFloat() > 0.985) {
                            //jump
                            this.yVel = 0.093f;
                            rabbit.startJumping();
                        }
                    }
                    //handles animation without using reflections
                    rabbit.livingTick();
                    break;
                case CAT:
                    CatEntity cat = (CatEntity) this.mob;
                    //cat.func_233687_w_(true);
                    cat.setSleeping(true);
                    break;
                //TODO: move jump position & stuff inside entity. merge with jar one
            }
        }
    }

    //only client side. cached mob from entitydata
    public void updateMob(){
        if(this.entityData.contains("id")) {
            Entity entity;
            if(this.entityData.get("id").getString().equals("minecraft:bee")){
                entity = new BeeEntity(EntityType.BEE, this.world);
            }
            else{
                entity  = EntityType.loadEntityAndExecute(this.entityData, this.world, o -> o);
            }
            if (entity==null)return;

            if(this.uuid!=null) {
                entity.setUniqueId(this.uuid);
            }

            //TODO: add shadows
            double px = this.pos.getX() + 0.5;
            double py = this.pos.getY() + 0.5 + 0.0625;
            double pz = this.pos.getZ() + 0.5;
            entity.setPosition(px, py, pz);
            //entity.setMotion(0,0,0);
            entity.lastTickPosX = px;
            entity.lastTickPosY = py;
            entity.lastTickPosZ = pz;
            entity.prevPosX = px;
            entity.prevPosY = py;
            entity.prevPosZ = pz;

            this.mob = entity;
            this.animationType = JarMobType.getJarMobType(entity);
            this.entityChanged = false;
        }
    }

    public boolean hasNoMob(){
        return this.entityData==null;
    }


}