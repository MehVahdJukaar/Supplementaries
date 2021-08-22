package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob.AnimationCategory;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper.CapturedMobProperties;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.UUID;

//TODO: rewrite and clean up this mess
public class MobHolder {
    private final Random rand = new Random();

    private World world;
    private BlockPos pos;

    private ItemStack bucketHolder = ItemStack.EMPTY;
    public SpecialBehaviorType specialBehaviorType = SpecialBehaviorType.NONE;
    public CapturedMobProperties capturedMobProperties = CapturedMobsHelper.DEFAULT;

    private boolean firstTick = true;

    public CompoundNBT entityData = null;
    public UUID uuid = null;
    public float yOffset = 1;
    public float scale = 1;
    public String name;
    //client only
    public Entity mob = null;
    public float jumpY = 0;
    public float prevJumpY = 0;
    public float yVel = 0;

    public MobHolder(World world, BlockPos pos){
        this.world = world;
        this.pos = pos;
    }

    public void setWorldAndPos(World world, BlockPos pos){
        this.world = world;
        this.pos = pos;
    }

    public void setPartying(BlockPos pos, boolean isPartying){
        if(this.mob!=null && this.mob instanceof LivingEntity){
            ((LivingEntity)this.mob).setRecordPlayingNearby(pos, isPartying);
        }
    }

    public void read(CompoundNBT compound) {
        if(compound.contains("MobHolder")){
            CompoundNBT cmp = compound.getCompound("MobHolder");
            this.entityData = cmp.getCompound("EntityData");
            this.scale = cmp.getFloat("Scale");
            this.yOffset = cmp.getFloat("YOffset");
            //this.specialBehaviorType = SpecialBehaviorType.values()[cmp.getInt("AnimationType")];
            if(cmp.contains("UUID")) this.uuid = cmp.getUUID("UUID");
            this.name = cmp.getString("Name");
        }
        if(compound.contains("BucketHolder")){
            this.bucketHolder = ItemStack.of(compound.getCompound("BucketHolder"));
        }
    }

    public CompoundNBT write(CompoundNBT compound) {
        if(this.entityData!=null) {
            int fishTexture = mob instanceof WaterMobEntity? -69:this.capturedMobProperties.getFishTexture();
            saveMobToNBT(compound, this.entityData, this.scale, this.yOffset, this.name, this.uuid, fishTexture);
        }
        if(!this.bucketHolder.isEmpty()){
            int fishTexture = mob instanceof WaterMobEntity? -69:this.capturedMobProperties.getFishTexture();
            saveBucketToNBT(compound, this.bucketHolder, this.name, fishTexture);
        }
        return compound;
    }

    public static void saveBucketToNBT(CompoundNBT compound, ItemStack bucket, String name, int fishTexture){
        CompoundNBT cmp = new CompoundNBT();
        bucket.save(cmp);
        if(fishTexture>=0||fishTexture==-69)
            cmp.putInt("FishTexture",fishTexture);
        if(name!=null) cmp.putString("Name",name);
        compound.put("BucketHolder", cmp);
    }

    public static void saveMobToNBT(CompoundNBT compound, CompoundNBT entityData, float scale,
                                    float yOffset, String name, UUID id, int fishTexture){
        CompoundNBT cmp = new CompoundNBT();
        cmp.put("EntityData", entityData);
        cmp.putFloat("Scale", scale);
        cmp.putFloat("YOffset", yOffset);
        if (id != null) cmp.putUUID("UUID", id);
        cmp.putString("Name", name);
        if (fishTexture >= 0 || fishTexture == -69)
            cmp.putInt("FishTexture", fishTexture);
        compound.put("MobHolder", cmp);
    }

    public boolean acceptFishBucket(ItemStack stack){
        Item item = stack.getItem();
        /*
        if(CapturedMobs.VALID_BUCKETS.contains(item)) {
            this.bucketHolder = stack.copy();
            if (world instanceof ServerWorld) {
                EntityType<?> entityType = null;
                try {
                    Field f = ObfuscationReflectionHelper.findField(FishBucketItem.class, "type");
                    f.setAccessible(true);
                    entityType = (EntityType<?>) f.get(item);
                } catch (Exception exception) {
                    try {
                        Field f = ObfuscationReflectionHelper.findField(FishBucketItem.class, "getFishType");
                        f.setAccessible(true);
                        entityType = (EntityType<?>) f.get(item);
                    } catch (Exception ignored) {}
                }
                if (entityType != null) {
                    Entity entity = entityType.create((ServerWorld) world, stack.getTag(), stack.hasDisplayName() ? stack.getDisplayName() : null, null, pos, SpawnReason.BUCKET, false, false);
                    if (entity instanceof AbstractFishEntity) ((AbstractFishEntity) entity).setFromBucket(true);
                    this.read(createMobHolderItemNBT(entity, EmptyCageItem.CageWhitelist.JAR.height, EmptyCageItem.CageWhitelist.JAR.width));
                    this.init();
                }
            }
            return true;
        }
        */
        return false;
    }

    //only called from jar
    public boolean interactWithBucketItem(ItemStack stack, @Nullable PlayerEntity player, Hand hand){
        Item item = stack.getItem();

        ItemStack returnStack = ItemStack.EMPTY;
        //fill
        if(CapturedMobsHelper.isFishBucket(item) && this.isEmpty()) {
            this.bucketHolder = stack.copy();
            this.world.playSound(null, this.pos, SoundEvents.BUCKET_EMPTY_FISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            returnStack = new ItemStack(Items.BUCKET);
        }
        //empty
        else if(!this.bucketHolder.isEmpty() && item == Items.BUCKET ){
            this.world.playSound(null, this.pos, SoundEvents.BUCKET_FILL_FISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            returnStack = this.bucketHolder.copy();
            this.clear();
        }
        if(!returnStack.isEmpty()) {
            if (player != null) {
                player.awardStat(Stats.ITEM_USED.get(item));
                if (!player.isCreative()) {
                    Utils.swapItem(player, hand, returnStack);
                }
            }
            return true;
        }
        return false;
    }


    public void clear(){
        this.bucketHolder = ItemStack.EMPTY;
        this.firstTick = true;
        this.mob = null;
        this.entityData = null;
    }

    public void tick() {

        if(this.firstTick && !this.isEmpty()){
            this.init();
            this.firstTick = false;
        }

        if (this.mob == null)return;

        //needed for eggs
        this.mob.tickCount++;

        //interface stuff
        boolean hasCustomMethods = this.mob instanceof ICatchableMob;
        if(hasCustomMethods) ((ICatchableMob) this.mob).tickInsideCageOrJar();


        if (!this.world.isClientSide) {
            if (this.specialBehaviorType == SpecialBehaviorType.CHICKEN) {
                ChickenEntity ch = (ChickenEntity) this.mob;
                if (--ch.eggTime <= 0) {
                    ch.spawnAtLocation(Items.EGG);
                    ch.eggTime = this.rand.nextInt(6000) + 6000;
                }
            }
            else if(this.specialBehaviorType==SpecialBehaviorType.SQUID){
                ((LivingEntity) this.mob).aiStep();
            }
        }
        else {
            //client side animation
            this.prevJumpY = this.jumpY;
            switch (this.specialBehaviorType) {
                default:
                case NONE:
                    break;
                case SLIME:
                    SlimeEntity slime = (SlimeEntity) this.mob;
                    slime.squish += (slime.targetSquish - slime.squish) * 0.5F;
                    slime.oSquish = slime.squish;
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
                            slime.targetSquish = -0.5f;
                        }
                        if (this.rand.nextFloat() > 0.985) {
                            //jump
                            this.yVel = 0.08f;
                            slime.targetSquish = 1.0F;
                        }
                    }
                    slime.targetSquish *= 0.6F;
                    break;
                case ENDERMITE:
                    if (this.rand.nextFloat() > 0.7f) {
                        this.world.addParticle(ParticleTypes.PORTAL, this.pos.getX() + 0.5f, this.pos.getY() + 0.2f,
                                this.pos.getZ() + 0.5f, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
                    }
                    break;
                case PARROT:
                    ((LivingEntity) this.mob).aiStep();
                    boolean p = ((ParrotEntity) this.mob).isPartyParrot();
                    this.mob.setOnGround(p);
                    this.jumpY = p ? 0 : 0.0625f;
                    break;
                case TICKABLE:
                    ((LivingEntity) this.mob).aiStep();
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
                    //handles actual animation without using reflections
                    rabbit.aiStep();
                    //TODO: living tick causes collisions to happen
                    break;
                case CAT:
                    CatEntity cat = (CatEntity) this.mob;
                    //cat.setOrderedToSit(true);
                    cat.setInSittingPose(true);
                    //this.jumpY=0.0325f;
                    break;
                case SQUID:
                    SquidEntity squid = (SquidEntity) this.mob;
                    squid.aiStep();
                    break;
                case CHICKEN:
                    ChickenEntity ch = (ChickenEntity) this.mob;
                    ch.aiStep();
                    if (rand.nextFloat() > (ch.isOnGround() ? 0.99 : 0.88)) ch.setOnGround(!ch.isOnGround());
                    break;
            }
            if (this.capturedMobProperties.isFloating() || (hasCustomMethods && ((ICatchableMob) this.mob).getAnimationCategory().isFloating())) {
                this.jumpY = 0.04f * MathHelper.sin(this.mob.tickCount / 10f) - 0.03f;
            }
        }
    }

    //TODO: react to fluid change
    public void setWaterMobInWater(boolean w){
        if(this.mob != null && this.mob instanceof WaterMobEntity && this.mob.isInWater()!=w){
            this.mob.wasTouchingWater = w;
        }
    }

    @Nullable
    public static Entity createEntityFromNBT(CompoundNBT com, UUID id, World world){
        if(com!=null && com.contains("id")) {
            Entity entity;
            //TODO: remove in 1.17
            String name = com.get("id").getAsString();
            switch (name) {
                case "minecraft:bee":
                    entity = new BeeEntity(EntityType.BEE, world);
                    break;
                case "minecraft:iron_golem":
                    entity = new IronGolemEntity(EntityType.IRON_GOLEM, world);
                    break;
                case "minecraft:enderman":
                    entity = new EndermanEntity(EntityType.ENDERMAN, world);
                    break;
                case "minecraft:wolf":
                    entity = new WolfEntity(EntityType.WOLF, world);
                    break;
                default:
                    entity = EntityType.loadEntityRecursive(com, world, o -> o);
                    break;
            }
            if(id!=null && entity!=null){
                entity.setUUID(id);
            }
            return entity;
        }
        return null;
    }

    //called on the first tick after the tile received the nbt data from the item
    //can't be client only because It needs to generate a mob
    //client and server. cached mob from entitydata
    //sets the entity parameters to with with current position
    public void init(){
        //this has two mode: normal and bucket mode
        if(!this.bucketHolder.isEmpty()){
            this.capturedMobProperties = CapturedMobsHelper.getTypeFromBucket(this.bucketHolder.getItem());
            if(this.name==null||this.name.isEmpty()) this.name = CapturedMobsHelper.getDefaultNameFromBucket(this.bucketHolder.getItem());
        }
        else if(this.world!=null && this.entityData!=null) {

            Entity entity = createEntityFromNBT(this.entityData, this.uuid, this.world);
            if (entity == null) return;

            //don't even need to sync these
            this.specialBehaviorType = SpecialBehaviorType.getType(entity);
            this.capturedMobProperties = CapturedMobsHelper.getType(entity);
            //this.setBucketHolder(entity);

            //client side stuff

            //TODO: add shadows
            double px = this.pos.getX() + 0.5;
            double py = this.pos.getY() + (0.5 + 0.0625) + 0.5;
            double pz = this.pos.getZ() + 0.5;
            entity.setPos(px, py, pz);
            //entity.setMotion(0,0,0);
            entity.xOld = px;
            entity.yOld = py;
            entity.zOld = pz;
            entity.xo = px;
            entity.yo = py;
            entity.zo = pz;
            entity.tickCount += this.rand.nextInt(40);

            //server doesn't need this
            this.mob = entity;
            //TODO: make properly react to water
            this.setWaterMobInWater(true); //!this.world.getFluidState(pos).isEmpty()
            if (!this.world.isClientSide) {
                int light;
                if(this.mob instanceof ICatchableMob){
                    light = ((ICatchableMob) this.mob).getLightLevel();
                }
                else{
                    light = this.capturedMobProperties.getLightLevel();
                }
                BlockState state = this.world.getBlockState(this.pos);
                if (state.getValue(BlockProperties.LIGHT_LEVEL_0_15) != light) {
                    this.world.setBlock(this.pos, state.setValue(BlockProperties.LIGHT_LEVEL_0_15, light), 2 | 4 | 16);
                }
            }
        }
    }

    //todo: delete this
    //wtf is this.
    //this is horrible
    private void setBucketHolder(Entity entity){
        /*
        public net.minecraft.entity.passive.fish.AbstractFishEntity func_203707_dx()Lnet/minecraft/item/ItemStack;

        public net.minecraft.entity.passive.fish.AbstractFishEntity func_204211_f(Lnet/minecraft/item/ItemStack;)V
        public net.minecraft.entity.passive.fish.TropicalFishEntity func_204211_f(Lnet/minecraft/item/ItemStack;)V*/
        if(entity instanceof AbstractFishEntity){

            try {
                Method m = ObfuscationReflectionHelper.findMethod(AbstractFishEntity.class, "func_203707_dx");
                m.setAccessible(true);
                this.bucketHolder = (ItemStack) m.invoke(entity);
            } catch (Exception exception) {
                try {
                    Method m = ObfuscationReflectionHelper.findMethod(AbstractFishEntity.class, "getFishBucket");
                    m.setAccessible(true);
                    this.bucketHolder = (ItemStack) m.invoke(entity);
                } catch (Exception ignored) {}
            }
            try{
                Method m2 = ObfuscationReflectionHelper.findMethod(AbstractFishEntity.class, "func_204211_f", ItemStack.class);
                m2.setAccessible(true);
                m2.invoke(entity,this.bucketHolder);
            } catch (Exception exception) {
                try{
                    Method m2 = ObfuscationReflectionHelper.findMethod(AbstractFishEntity.class, "setBucketData", ItemStack.class);
                    m2.setAccessible(true);
                    m2.invoke(entity,this.bucketHolder);
                } catch (Exception ignored) {}
            }
        }
    }

    public boolean isEmpty(){
        return this.entityData==null && this.bucketHolder.isEmpty();

    }

    private static boolean isInAir(Entity mob, ICatchableMob.AnimationCategory category){
        return !category.isLand() && (category.isFlying() || mob.isNoGravity() || mob instanceof IFlyingAnimal ||
                mob.isIgnoringBlockTriggers() || mob instanceof WaterMobEntity);
    }

    //called by the item. turns a mob into what's store inside item and tile
    @Nullable
    public static CompoundNBT createMobHolderItemNBT(Entity mob, float blockW, float blockH){
        if(mob==null)return null;
        if(mob instanceof LivingEntity){
            LivingEntity le = (LivingEntity) mob;
            le.yHeadRotO = 0;
            le.yHeadRot = 0;
            le.animationSpeed = 0;
            le.animationSpeedOld = 0;
            le.animationPosition = 0;
            le.hurtDuration=0;
            le.hurtTime=0;
            le.attackAnim=0;
        }
        if(mob instanceof AbstractFishEntity){
            ((AbstractFishEntity) mob).setFromBucket(true);
        }
        mob.yRot = 0;
        mob.yRotO = 0;
        mob.xRotO = 0;
        mob.xRot = 0;
        mob.clearFire();
        mob.invulnerableTime=0;

        UUID id = mob.getUUID();

        CompoundNBT mobCompound = new CompoundNBT();
        mob.save(mobCompound);
        if (!mobCompound.isEmpty()) {

            mobCompound.remove("Passengers");
            mobCompound.remove("Leash");
            mobCompound.remove("UUID");

            //TODO: improve for aquatic entities to react and not fly when not in water
            CapturedMobProperties mobProperties = CapturedMobsHelper.getType(mob);
            AnimationCategory category = mobProperties.getCategory();
            boolean isAir = isInAir(mob,category);


            float babyScale = 1;
            //non ageable
            if(mob instanceof AgeableEntity && ((LivingEntity) mob).isBaby()) babyScale = 2f;
            if(mobCompound.contains("IsBaby")&&mobCompound.getBoolean("IsBaby")||
                    (mob instanceof VillagerEntity && ((LivingEntity) mob).isBaby())) babyScale = 1.125f;

            float s = 1;
            float w = mob.getBbWidth() *babyScale;
            float h = mob.getBbHeight() *babyScale;
            //float maxh = isAir ? 0.5f : 0.75f;
            //1 px border

            float addWidth;
            float addHeight;
            if(mob instanceof ICatchableMob){
                addWidth = ((ICatchableMob) mob).getHitBoxWidthIncrement();
                addHeight = ((ICatchableMob) mob).getHitBoxHeightIncrement();
            }
            else{
                addWidth = mobProperties.getWidth();
                addHeight = mobProperties.getHeight();
            }

            float maxh = blockH - (isAir ? 0.25f : 0.125f) - addHeight;
            float maxw = blockW - 0.25f - addWidth;
            if (w > maxw || h > maxh) {
                if (w - maxw > h - maxh)
                    s = maxw / w;
                else
                    s = maxh / h;
            }
            //TODO: rewrite this to account for adjValues
            float y = isAir ? (blockH/2f) - h * s / 2f : 0.0626f;

            //ice&fire dragons
            String name = mob.getType().getRegistryName().toString();
            if(name.equals("iceandfire:fire_dragon")||name.equals("iceandfire:ice_dragon")||name.equals("iceandfire:lightning_dragon")){
                s*=0.45;
            }
            CompoundNBT cmp = new CompoundNBT();
            //TODO: stop coding like this omg
            int fishTexture = mob instanceof WaterMobEntity? -69: CapturedMobsHelper.getType(name).getFishTexture();
            saveMobToNBT(cmp, mobCompound, s, y, mob.getName().getString(), id, fishTexture);
            return cmp;
        }
        return null;
    }

    public boolean shouldHaveWater(){
        return this.specialBehaviorType.hasWater()||this.capturedMobProperties.isFish();
    }

    //for hardcoded special behaviors
    public enum SpecialBehaviorType {
        NONE,
        SLIME,
        VEX,
        ENDERMITE,
        PARROT,
        CAT,
        RABBIT,
        CHICKEN,
        TICKABLE,
        WATER_MOB,
        SQUID;

        public boolean hasWater(){
            return this==WATER_MOB||this==SQUID;
        }

        public static SpecialBehaviorType getType(Entity e){
            if(e instanceof SquidEntity)return SQUID;
            else if(e instanceof WaterMobEntity)return WATER_MOB;
            else if(e instanceof SlimeEntity)return SLIME;
            else if(e instanceof VexEntity)return VEX;

            else if(e instanceof ParrotEntity)return PARROT;
            else if(e instanceof CatEntity)return CAT;
            else if(e instanceof RabbitEntity)return RABBIT;
            else if(e instanceof ChickenEntity)return CHICKEN;
            else if(e.getType().getRegistryName().toString().equals("iceandfire:pixe")||
                    e.getType().getRegistryName().toString().equals("druidcraft:moth"))return TICKABLE;
            else if(e instanceof EndermiteEntity)return ENDERMITE;
            return SpecialBehaviorType.NONE;
        }
    }

}