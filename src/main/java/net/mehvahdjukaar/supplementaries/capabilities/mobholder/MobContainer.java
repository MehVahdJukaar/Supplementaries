package net.mehvahdjukaar.supplementaries.capabilities.mobholder;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.capabilities.CapabilityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class MobContainer {

    private final float width;
    private final float height;

    //stuff that actually gets saved
    @Nullable
    private MobData data;

    //static mob instance created from entity data. contained in the capability that handles the animation
    @Nullable
    private ICatchableMob mobDisplayCapInstance;

    public MobContainer(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public static <E extends Entity> ICatchableMob getCap(E entity) {
        if (entity == null) return null;
        ICatchableMob cap;
        if (entity instanceof ICatchableMob) cap = (ICatchableMob) entity;
        else {
            LazyOptional<ICatchableMob> opt = entity.getCapability(CapabilityHandler.CATCHABLE_MOB_CAP);
            cap = opt.orElseGet(() -> DefaultCatchableMobCap.getDefaultCap(entity));
        }
        return cap;
    }

    public CompoundTag save(CompoundTag tag) {
        if (!this.isEmpty()) {
            this.data.saveToTag(tag);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        MobData data = MobData.loadFromTag(tag);
        this.setData(data);
    }

    private void setData(@Nullable MobData data) {
        this.data = data;
        this.needsInitialization = true;
    }

    //----init----

    private boolean needsInitialization = false;

    private void initializeEntity(Level level, BlockPos pos) {
        this.needsInitialization = false;
        if (data != null && level != null && pos != null && !data.isAquarium) {
            Entity entity = createStaticMob(data, level, pos);

            if(entity != null) {
                //visual entity stored in capability
                this.mobDisplayCapInstance = getCap(entity);
                this.mobDisplayCapInstance.setContainerDimensions(this.width, this.height);
                this.mobDisplayCapInstance.onContainerWaterlogged(level.getFluidState(pos).getType() != Fluids.EMPTY);

                this.updateLightLevel(level, pos);
            }
        }
    }

    public void updateLightLevel(Level level, BlockPos pos) {
        int light;
        if (level != null && !level.isClientSide) {
            if (hasDisplayMob()) {
                light = mobDisplayCapInstance.getLightLevel();
            } else {
                light = CapturedMobsHelper.getTypeFromBucket(this.data.filledBucket.getItem()).getLightLevel();
            }
            BlockState state = level.getBlockState(pos);
            if (state.getValue(BlockProperties.LIGHT_LEVEL_0_15) != light) {
                level.setBlock(pos, state.setValue(BlockProperties.LIGHT_LEVEL_0_15, light), 2 | 4 | 16);
            }
        }
    }


    /**
     * initialize mob holder when loaded. creates a static entity for rendering. serverside too since we need it for mobs like chicken which need to lay eggs
     */
    //TODO: make this holder store an actual mob so one can modify it when inside the container. in other words save this entity to this mob data when done
    @Nullable
    public static Entity createStaticMob(MobData data, @Nonnull Level world, BlockPos pos) {
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
            }
        }
        return entity;
    }

    @Nullable
    public static Entity createEntityFromNBT(CompoundTag tag, @Nullable UUID id, Level world) {
        if (tag != null && tag.contains("id")) {
            Entity entity = EntityType.loadEntityRecursive(tag, world, o -> o);
            if (id != null && entity != null) {
                entity.setUUID(id);
                if(entity.hasCustomName())entity.setCustomName(entity.getCustomName());
            }
            return entity;
        }
        return null;
    }

    //-----end-init-----


    //TODO: dispensers
    //only called from jar. move to mob container
    public boolean interactWithBucket(ItemStack stack, Level world, BlockPos pos, @Nullable Player player, InteractionHand hand) {
        Item item = stack.getItem();

        ItemStack returnStack = ItemStack.EMPTY;
        //fill
        if (this.isEmpty()) {
            if (CapturedMobsHelper.isFishBucket(item)) {

                world.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                returnStack = new ItemStack(Items.BUCKET);

                MobData data = new MobData(stack.copy());
                this.setData(data);
            }
        } else {

            //empty
            if (!this.data.filledBucket.isEmpty() && item == Items.BUCKET) {
                world.playSound(null, pos, SoundEvents.BUCKET_FILL_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
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

    public boolean isEmpty() {
        return this.data == null;
    }

    public boolean hasDisplayMob() {
        return this.mobDisplayCapInstance != null;
    }

    public void tick(Level pLevel, BlockPos pPos) {
        if (this.needsInitialization) this.initializeEntity(pLevel, pPos);
        if (this.hasDisplayMob()) {
            //TODO: maybe put inside cap
            this.mobDisplayCapInstance.getEntity().tickCount++;
            this.mobDisplayCapInstance.tickInsideContainer(pLevel, pPos, this.data.scale, this.data.mobTag);
        }
    }

    public InteractionResult onInteract(Level world, BlockPos pos, Player player, InteractionHand hand) {
        if (this.hasDisplayMob()) {
            return mobDisplayCapInstance.onPlayerInteract(world, pos, player, hand, this.data.mobTag);
        }
        return InteractionResult.PASS;
    }

    public @Nullable
    MobData getData() {
        return data;
    }

    @Nullable
    public Entity getDisplayedMob() {
        if (this.hasDisplayMob()) {
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
    public static CompoundTag createMobHolderItemTag(@Nonnull Entity mob, float blockW, float blockH, ItemStack bucketStack, boolean isAquarium) {

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

            CompoundTag mobTag = prepareMobTagForContainer(mob);
            if (mobTag == null) return null;


            UUID id = mob.getUUID();

            data = new MobData(name, mobTag, scale, id, bucketStack);
        }

        CompoundTag cmp = new CompoundTag();
        data.saveToTag(cmp);

        return cmp;
    }

    /**
     * prepares the entity nbt to be stored in a MobHolder (Item and Block)
     *
     * @param entity entity
     * @return entity tag
     */
    @Nullable
    private static CompoundTag prepareMobTagForContainer(Entity entity) {

        if (entity.isPassenger()) {
            entity.getVehicle().ejectPassengers();
        }
        if(entity instanceof Mob mob){
            mob.setPersistenceRequired();
        }
        if(entity instanceof Bucketable bucketable){
            bucketable.setFromBucket(true);
        }

        //prepares entity
        if (entity instanceof LivingEntity le) {
            le.yHeadRotO = 0;
            le.yHeadRot = 0;
            le.animationSpeed = 0;
            le.animationSpeedOld = 0;
            le.animationPosition = 0;
            le.hurtDuration = 0;
            le.hurtTime = 0;
            le.attackAnim = 0;
        }
        entity.setYRot(0);
        entity.yRotO = 0;
        entity.xRotO = 0;
        entity.setXRot(0);
        entity.clearFire();
        entity.invulnerableTime = 0;

        if (entity instanceof Bat) {
            ((Bat) entity).setResting(true);
        }
        if (entity instanceof Fox) {
            ((Fox) entity).setSleeping(true);
        }
        if (entity instanceof AbstractFish) {
            ((AbstractFish) entity).setFromBucket(true);
        }

        CompoundTag mobTag = new CompoundTag();
        entity.save(mobTag);

        if (mobTag.isEmpty()) {
            Supplementaries.LOGGER.error("failed to capture entity " + entity + "Something went wrong :/");
            return null;
        }

        mobTag.remove("Passengers");
        mobTag.remove("Leash");
        mobTag.remove("UUID");
        if (mobTag.contains("FromBucket")) {
            mobTag.putBoolean("FromBucket", true);
        }
        if (mobTag.contains("FromPot")) {
            mobTag.putBoolean("FromPot", true);
        }

        return mobTag;
    }

    /**
     * get mob scale and vertical offset for a certain container
     *
     * @param cap    mob capability    entity
     * @param blockW container width
     * @param blockH container height
     * @return scale and y offset
     */
    public static Pair<Float, Float> calculateMobDimensionsForContainer(ICatchableMob cap, float blockW, float blockH, boolean waterlogged) {

        Entity mob = cap.getEntity();

        float babyScale = 1;

        if (mob instanceof LivingEntity && ((LivingEntity) mob).isBaby()) {
            if ((mob instanceof Villager)) babyScale = 1.125f;
            else if (mob instanceof AgeableMob) babyScale = 2f;
            else babyScale = 1.125f;
        }

        float scale = 1;

        float w = mob.getBbWidth() * babyScale;
        float h = mob.getBbHeight() * babyScale;

        cap.getHitBoxHeightIncrement();

        boolean isAir = cap.isFlyingMob(waterlogged);

        float aW = w + cap.getHitBoxWidthIncrement();
        float aH = h + cap.getHitBoxHeightIncrement();

        //1 pixel margin
        float margin = 1 / 16f * 2;
        float yMargin = 1 / 16f;

        float maxH = blockH - 2 * (isAir ? margin : yMargin);
        float maxW = blockW - 2 * margin;
        //if width and height are greater than maximum allowed vales for container scale down
        if (aW > maxW || aH > maxH) {
            if (aW - maxW > aH - maxH)
                scale = maxW / aW;
            else
                scale = maxH / aH;
        }
        //ice&fire dragons
        String name = mob.getType().getRegistryName().toString();
        if (name.equals("iceandfire:fire_dragon") || name.equals("iceandfire:ice_dragon") || name.equals("iceandfire:lightning_dragon")) {
            scale *= 0.45;
        }

        float yOffset = isAir ? (blockH / 2f) - aH * scale / 2f : yMargin;

        if (mob instanceof Bat) {
            yOffset *= 1.5f;
        }

        return new ImmutablePair<>(scale, yOffset);
    }

    public void clear() {
        this.data = null;
        this.mobDisplayCapInstance = null;
    }

    public static class MobData {
        public final String name;
        public final boolean isAquarium;
        private final ItemStack filledBucket;

        public final CompoundTag mobTag;
        private final float scale;
        @Nullable
        private final UUID uuid;

        private final int fishIndex;


        public MobData(String name, CompoundTag mobTag, float scale, @Nullable UUID id, ItemStack filledBucket) {
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
        public static MobData loadFromTag(CompoundTag tag) {
            if (tag.contains("MobHolder")) {
                CompoundTag cmp = tag.getCompound("MobHolder");
                CompoundTag entityData = cmp.getCompound("EntityData");
                float scale = cmp.getFloat("Scale");
                UUID uuid = cmp.contains("UUID") ? cmp.getUUID("UUID") : null;
                ItemStack bucket = cmp.contains("Bucket") ? ItemStack.of(cmp.getCompound("Bucket")) : ItemStack.EMPTY;
                String name = cmp.getString("Name");

                //backwards compat
                if (cmp.contains("YOffset")) {
                    float y = cmp.getFloat("YOffset");
                    ListTag listnbt = new ListTag();
                    listnbt.add(DoubleTag.valueOf(0.5));
                    listnbt.add(DoubleTag.valueOf(y));
                    listnbt.add(DoubleTag.valueOf(0.5));

                    if (entityData.contains("Pos")) entityData.remove("Pos");
                    entityData.put("Pos", listnbt);
                }

                return new MobData(name, entityData, scale, uuid, bucket);
            }
            if (tag.contains("BucketHolder")) {
                CompoundTag cmp = tag.getCompound("BucketHolder");
                ItemStack bucket = ItemStack.of(cmp.getCompound("Bucket"));
                int fish = cmp.getInt("FishTexture");
                String name = cmp.getString("Name");
                return new MobData(name, fish, bucket);
            }
            //Supplementaries.LOGGER.error("Invalid tile entity data for mob holder");
            return null;
        }

        public void saveToTag(CompoundTag tag) {
            CompoundTag cmp = new CompoundTag();
            cmp.putString("Name", name);
            if (!filledBucket.isEmpty() || this.isAquarium) {
                CompoundTag bucketTag = new CompoundTag();
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