package net.mehvahdjukaar.supplementaries.common.mobholder;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.capabilities.SupplementariesCapabilities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

//after rewriting this mess it somehow ended up even worse... HOW??
//edit: third time the charm?
//edit forth?
//TODO: maybe remove type here since I'm not using it anymore
public class MobContainer {

    private final float width;
    private final float height;

    @Nullable
    private World world;
    private BlockPos pos;

    //stuff that actually gets saved
    @Nullable
    private MobData data;

    //static mob instance created from entity data. contained in the capability that handles the animation
    @Nullable
    private ICatchableMob mobDisplayCapInstance;

    //TODO: maybe make this not null so it can use non static save and load
    public MobContainer(float width, float height, @Nullable World world, BlockPos pos) {
        this.width = width;
        this.height = height;
        this.world = world;
        this.pos = pos;
    }

    //call on load
    public void setWorldAndPos(World world, BlockPos pos){
        this.pos = pos;
        this.world = world;
    }

    public void updateLightLevel() {
        int light;
        if (world != null && !world.isClientSide) {
            if (hasDisplayMob()) {
                light = mobDisplayCapInstance.getLightLevel();
            } else {
                light = CapturedMobsHelper.getTypeFromBucket(this.data.filledBucket.getItem()).getLightLevel();
            }

            BlockState state = this.world.getBlockState(this.pos);
            if (state.getValue(BlockProperties.LIGHT_LEVEL_0_15) != light) {
                this.world.setBlock(this.pos, state.setValue(BlockProperties.LIGHT_LEVEL_0_15, light), 2 | 4 | 16);
            }
        }
    }

    public static <E extends Entity> ICatchableMob getCap(E entity) {
        if (entity == null) return null;
        ICatchableMob cap;
        if (entity instanceof ICatchableMob) cap = (ICatchableMob) entity;
        else {
            LazyOptional<ICatchableMob> opt = entity.getCapability(SupplementariesCapabilities.CATCHABLE_MOB_CAP);
            cap = opt.orElseGet(() -> DefaultCatchableMobCap.getDefaultCap(entity));
        }
        return cap;
    }

    public CompoundNBT save(CompoundNBT tag) {
        if(!this.isEmpty()) {
            this.data.saveToTag(tag);
        }
        return tag;
    }

    public void load(CompoundNBT tag) {
        MobData data = MobData.loadFromTag(tag);
        this.setData(data);
    }

    private void setData(@Nullable MobData data){
        this.data = data;
        this.needsInitialization = true;
    }

    private boolean needsInitialization = false;

    private void initializeEntity(){
        this.needsInitialization = false;
        if (data != null && this.world != null && this.pos != null && !data.isAquarium) {
            Entity entity = createStaticMob(data, world, pos);

            if(entity != null) {
                //visual entity stored in capability
                this.mobDisplayCapInstance = getCap(entity);
                this.mobDisplayCapInstance.setContainerDimensions(this.width, this.height);
                this.mobDisplayCapInstance.onContainerWaterlogged(world.getFluidState(pos).getType() != Fluids.EMPTY);

                this.updateLightLevel();
            }
        }
    }

    //----init----

    /**
     * initialize mob holder when loaded. creates a static entity for rendering. serverside too since we need it for mobs like chicken which need to lay eggs
     */
    //TODO: make this holder store an actual mob so one can modify it when inside the container. in other words save this entity to this mob data when done
    @Nullable
    public static Entity createStaticMob(MobData data, @Nonnull World world, BlockPos pos) {
        Entity entity = null;
        if (data != null) {

            //this has two mode: normal and bucket mode
            // if fish tank no visual mob is needed. aquarium has null entity
            if (!data.isAquarium && data.mobTag != null) {

                entity = createEntityFromNBT(data.mobTag, data.uuid, world);
                if (entity == null) return null;

                //don't even need to sync these since they are only used by the block

                //sets the correct mob position to its block pos
                double px = pos.getX() + entity.getX();
                double py = pos.getY() + entity.getY();
                double pz = pos.getZ() + entity.getZ();

                entity.setPos(px, py, pz);
                //entity.setMotion(0,0,0);
                entity.xOld = px;
                entity.yOld = py;
                entity.zOld = pz;
                entity.xo = px;
                entity.yo = py;
                entity.zo = pz;
                //entity.tickCount += this.rand.nextInt(40);

                //TODO: make properly react to water
                //this.setWaterMobInWater(true); //!this.world.getFluidState(pos).isEmpty()
            }
        }
        return entity;
    }

    @Nullable
    public static Entity createEntityFromNBT(CompoundNBT com, @Nullable UUID id, World world) {
        if (com != null && com.contains("id")) {
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
            if (id != null && entity != null) {
                entity.setUUID(id);
            }
            return entity;
        }
        return null;
    }

    //-----end-init-----


    //TODO: dispensers
    //only called from jar. move to mob container
    public boolean interactWithBucket(ItemStack stack, World world, BlockPos pos, @Nullable PlayerEntity player, Hand hand) {
        Item item = stack.getItem();

        ItemStack returnStack = ItemStack.EMPTY;
        //fill
        if (this.isEmpty()) {
            if (CapturedMobsHelper.isFishBucket(item)) {

                world.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                returnStack = new ItemStack(Items.BUCKET);

                MobData data = new MobData(stack.copy());
                this.setData(data);
            }
        } else {

            //empty
            if (!this.data.filledBucket.isEmpty() && item == Items.BUCKET) {
                world.playSound(null, pos, SoundEvents.BUCKET_FILL_FISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                returnStack = this.data.filledBucket.copy();

                this.setData(null);
            }
        }

        if (!returnStack.isEmpty()) {
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


    public boolean isEmpty(){
        return this.data == null;
    }

    public boolean hasDisplayMob() {
        return this.mobDisplayCapInstance != null;
    }

    public void tick() {
        if(this.needsInitialization) this.initializeEntity();
        if (this.hasDisplayMob()) {
            //TODO: maybe put inside cap
            this.mobDisplayCapInstance.getEntity().tickCount++;
            this.mobDisplayCapInstance.tickInsideContainer(this.world, this.pos, this.data.scale, this.data.mobTag);
        }
    }

    public ActionResultType onInteract(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        if (this.hasDisplayMob()) {
            return mobDisplayCapInstance.onPlayerInteract(world, pos, player, hand, this.data.mobTag);
        }
        return ActionResultType.PASS;
    }

    public @Nullable MobData getData() {
        return data;
    }

    @Nullable
    public Entity getDisplayedMob() {
        if(this.hasDisplayMob()){
            return this.mobDisplayCapInstance.getEntity();
        }
        return null;
    }

    public boolean shouldHaveWater() {
        return this.data != null && this.data.isAquarium || (this.hasDisplayMob() && this.mobDisplayCapInstance.shouldHaveWater());
    }

    //item stuff

    /**
     * creates the Tag to be given to a mobHolder item upon interacting with a mob
     *
     * @param mob         entity to be captured
     * @param blockW      container width
     * @param blockH      contained height
     * @param bucketStack optional filled bucket item
     * @param isAquarium  if the container only needs to keep bucket data when bucket is not empty
     * @return item Tag
     */
    @Nullable
    public static CompoundNBT createMobHolderItemTag(@Nonnull Entity mob, float blockW, float blockH, ItemStack bucketStack, boolean isAquarium) {

        MobData data;
        String name = mob.getName().getString();
        if (isAquarium && CapturedMobsHelper.getType(mob).isFish()) {
            data = new MobData(name, bucketStack);
        } else {
            Pair<Float, Float> dimensions = calculateMobDimensionsForContainer(getCap(mob), blockW, blockH, false);

            float scale = dimensions.getLeft();
            float yOffset = dimensions.getRight();

            //set post relative to center block cage
            double px = 0.5;
            double py = yOffset + 0.0001;//+ 0.0625;
            double pz = 0.5;
            mob.setPos(px, py, pz);
            //entity.setMotion(0,0,0);
            mob.xOld = px;
            mob.yOld = py;
            mob.zOld = pz;

            CompoundNBT mobTag = prepareMobTagForContainer(mob);
            if (mobTag == null) return null;


            UUID id = mob.getUUID();

            data = new MobData(name, mobTag, scale, id, bucketStack);
        }

        CompoundNBT cmp = new CompoundNBT();
        data.saveToTag(cmp);

        return cmp;
    }

    /**
     * prepares the mob nbt to be stored in a MobHolder (Item and Block)
     *
     * @param mob entity
     * @return mob tag
     */
    @Nullable
    private static CompoundNBT prepareMobTagForContainer(Entity mob) {

        if (mob.isPassenger()) {
            mob.getVehicle().ejectPassengers();
        }

        //prepares mob
        if (mob instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) mob;
            le.yHeadRotO = 0;
            le.yHeadRot = 0;
            le.animationSpeed = 0;
            le.animationSpeedOld = 0;
            le.animationPosition = 0;
            le.hurtDuration = 0;
            le.hurtTime = 0;
            le.attackAnim = 0;
        }
        mob.yRot = 0;
        mob.yRotO = 0;
        mob.xRotO = 0;
        mob.xRot = 0;
        mob.clearFire();
        mob.invulnerableTime = 0;

        if (mob instanceof BatEntity) {
            ((BatEntity) mob).setResting(true);
        }
        if (mob instanceof FoxEntity) {
            ((FoxEntity) mob).setSleeping(true);
        }
        if (mob instanceof AbstractFishEntity) {
            ((AbstractFishEntity) mob).setFromBucket(true);
        }

        CompoundNBT mobTag = new CompoundNBT();
        mob.save(mobTag);

        if (mobTag.isEmpty()) {
            Supplementaries.LOGGER.error("failed to capture mob " + mob + "Something went wrong :/");
            return null;
        }

        mobTag.remove("Passengers");
        mobTag.remove("Leash");
        mobTag.remove("UUID");
        if (mobTag.contains("FromBucket")) {
            mobTag.putBoolean("FromBucket", true);
        }

        return mobTag;
    }

    /**
     * get mob scale and vertical offset for a certain container
     *
     * @param cap mob capability    entity
     * @param blockW container width
     * @param blockH container height
     * @return scale and y offset
     */
    public static Pair<Float, Float> calculateMobDimensionsForContainer(ICatchableMob cap, float blockW, float blockH, boolean waterlogged) {

        Entity mob = cap.getEntity();

        float babyScale = 1;

        if (mob instanceof LivingEntity && ((LivingEntity) mob).isBaby()) {
            if ((mob instanceof VillagerEntity)) babyScale = 1.125f;
            else if (mob instanceof AgeableEntity) babyScale = 2f;
            else babyScale = 1.125f;
        }

        float scale = 1;

        float w = mob.getBbWidth() * babyScale;
        float h = mob.getBbHeight() * babyScale;

        cap.getHitBoxHeightIncrement();

        boolean isAir = cap.isFlyingMob(waterlogged);


        float addWidth = cap.getHitBoxWidthIncrement();
        float addHeight = cap.getHitBoxHeightIncrement();

        //1 pixel margin
        float margin = 1 / 16f * 2;
        float yMargin = 1 / 16f;

        float maxH = blockH - 2 * (isAir ? margin : yMargin) - addHeight;
        float maxW = blockW - 2 * margin - addWidth;
        //if width and height are greater than maximum allowed vales for container scale down
        if (w > maxW || h > maxH) {
            if (w - maxW > h - maxH)
                scale = maxW / w;
            else
                scale = maxH / h;
        }
        //ice&fire dragons
        String name = mob.getType().getRegistryName().toString();
        if (name.equals("iceandfire:fire_dragon") || name.equals("iceandfire:ice_dragon") || name.equals("iceandfire:lightning_dragon")) {
            scale *= 0.45;
        }

        float yOffset = isAir ? (blockH / 2f) - h * scale / 2f : yMargin;

        if (mob instanceof BatEntity) {
            yOffset *= 1.5f;
        }

        return new ImmutablePair<>(scale, yOffset);
    }

    public static class MobData {
        public final String name;
        public final boolean isAquarium;
        private final ItemStack filledBucket;

        public final CompoundNBT mobTag;
        private final float scale;
        @Nullable
        private final UUID uuid;

        private final int fishIndex;


        public MobData(String name, CompoundNBT mobTag, float scale, @Nullable UUID id, ItemStack filledBucket) {
            this.isAquarium = false;
            this.name = name;
            this.mobTag = mobTag;
            this.scale = scale;
            this.uuid = id;
            this.filledBucket = filledBucket;

            this.fishIndex = 0;
        }

        public MobData(ItemStack filledBucket) {
            this(CapturedMobsHelper.getDefaultNameFromBucket(filledBucket.getItem()), filledBucket);
        }

        public MobData(String name, ItemStack filledBucket) {
            this(name, CapturedMobsHelper.getTypeFromBucket(filledBucket.getItem()).getFishTexture(), filledBucket);
        }

        public MobData(String name, int fishIndex, ItemStack filledBucket) {
            this.isAquarium = true;
            this.fishIndex = fishIndex;
            this.filledBucket = filledBucket;
            this.name = name;

            //unique to non aquarium ones
            this.uuid = null;
            this.scale = 1;
            this.mobTag = null;
        }

        @Nullable
        public static MobData loadFromTag(CompoundNBT tag) {
            if (tag.contains("MobHolder")) {
                CompoundNBT cmp = tag.getCompound("MobHolder");
                CompoundNBT entityData = cmp.getCompound("EntityData");
                float scale = cmp.getFloat("Scale");
                UUID uuid = cmp.contains("UUID") ? cmp.getUUID("UUID") : null;
                ItemStack bucket = cmp.contains("Bucket") ? ItemStack.of(cmp.getCompound("Bucket")) : ItemStack.EMPTY;
                String name = cmp.getString("Name");

                //backwards compat
                if(cmp.contains("YOffset")){
                    float y = cmp.getFloat("YOffset");
                    ListNBT listnbt = new ListNBT();
                    listnbt.add(DoubleNBT.valueOf(0.5));
                    listnbt.add(DoubleNBT.valueOf(y));
                    listnbt.add(DoubleNBT.valueOf(0.5));

                    if(entityData.contains("Pos")) entityData.remove("Pos");
                    entityData.put("Pos", listnbt);
                }

                return new MobData(name, entityData, scale, uuid, bucket);
            }
            if (tag.contains("BucketHolder")) {
                CompoundNBT cmp = tag.getCompound("BucketHolder");
                ItemStack bucket = ItemStack.of(cmp.getCompound("Bucket"));
                //TODO: backwards compat, remove
                if (bucket.isEmpty()) bucket = ItemStack.of(cmp.getCompound("BucketHolder"));
                int fish = cmp.getInt("FishTexture");
                String name = cmp.getString("Name");
                return new MobData(name, fish, bucket);
            }
            //Supplementaries.LOGGER.error("Invalid tile entity data for mob holder");
            return null;
        }

        public void saveToTag(CompoundNBT tag) {
            CompoundNBT cmp = new CompoundNBT();
            cmp.putString("Name", name);
            if (!filledBucket.isEmpty() || this.isAquarium) {
                CompoundNBT bucketTag = new CompoundNBT();
                filledBucket.save(bucketTag);
                cmp.put("Bucket", bucketTag);
            }
            if (this.isAquarium) {
                cmp.putInt("FishTexture", this.fishIndex);
                tag.put("BucketHolder", cmp);
            } else {
                cmp.put("EntityData", mobTag);
                cmp.putFloat("Scale", scale);
                if (uuid != null) cmp.putUUID("UUID", uuid);

                tag.put("MobHolder", cmp);
            }
        }

        public float getScale() {
            return scale;
        }

        public int getFishIndex() {
            return fishIndex;
        }
    }
}