package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.CapturedMobInstance;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

//I swear I changed this stuff from the ground up at least 5 times, and it just keeps getting messier somehow. It just needs to do so much...
public class MobContainer {

    private final float width;
    private final float height;
    private final boolean isAquarium;

    //stuff that actually gets saved
    @Nullable
    private MobNBTData data;
    //static mob instance created from entity data.
    //handles the animations. Also contains a reference to the entity properties and visual entity itself
    @Nullable
    private CapturedMobInstance mobInstance;
    //mob settings. In case of bucket holder these wil only consist of default cap or data ones
    private ICatchableMob mobProperties;
    private boolean needsInitialization = false;

    public MobContainer(float width, float height, boolean isAquarium) {
        this.width = width;
        this.height = height;
        this.isAquarium = isAquarium;
    }

    public CompoundTag save(CompoundTag tag) {
        if (this.data != null) {
            this.data.save(tag, this.isAquarium);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        MobNBTData data = MobNBTData.load(tag);
        this.setData(data);
    }

    private void setData(@Nullable MobNBTData data) {
        this.data = data;
        this.mobInstance = null;
        this.needsInitialization = true;
    }

    //----init----

    private void initializeEntity(Level level, BlockPos pos) {
        this.needsInitialization = false;
        if (data != null && level != null && pos != null) {
            if (data instanceof MobNBTData.Bucket bucketData) {
                var type = BucketHelper.getEntityTypeFromBucket(bucketData.filledBucket.getItem());
                this.mobProperties = CapturedMobHandler.getDataCap(type, true);
            } else if (data instanceof MobNBTData.Entity entityData) {
                Entity entity = createStaticMob(entityData, level, pos);
                if (entity != null) {
                    //visual entity stored in this instance
                    this.mobProperties = CapturedMobHandler.getCatchableMobCapOrDefault(entity);
                    this.mobInstance = mobProperties.createCapturedMobInstance(entity, this.width, this.height);
                    this.mobInstance.onContainerWaterlogged(level.getFluidState(pos).getType() != Fluids.EMPTY);

                    this.updateLightLevel(level, pos);
                }
            }
        }
    }

    public void updateLightLevel(Level level, BlockPos pos) {
        int light = 0;
        if (level != null && !level.isClientSide && data != null) {
            if (mobProperties != null) {
                light = mobProperties.getLightLevel(level, pos);
            }
            BlockState state = level.getBlockState(pos);
            if (state.getValue(ModBlockProperties.LIGHT_LEVEL_0_15) != light) {
                level.setBlock(pos, state.setValue(ModBlockProperties.LIGHT_LEVEL_0_15, light), 2 | 4 | 16);
            }
        }
    }


    /**
     * initialize mob holder when loaded. creates a static entity for rendering. serverside too since we need it for mobs like chicken which need to lay eggs
     */
    //TODO: make this holder store an actual mob so one can modify it when inside the container. in other words save this entity to this mob data when done
    @Nullable
    public static Entity createStaticMob(MobNBTData.Entity data, @Nonnull Level world, BlockPos pos) {
        Entity entity = null;
        if (data != null) {
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
        return entity;
    }

    @Nullable
    public static Entity createEntityFromNBT(CompoundTag tag, @Nullable UUID id, Level world) {
        if (tag != null && tag.contains("id")) {
            Entity entity = EntityType.loadEntityRecursive(tag, world, o -> o);
            if (id != null && entity != null) {
                entity.setUUID(id);
                if (entity.hasCustomName()) entity.setCustomName(entity.getCustomName());
            }
            return entity;
        }
        return null;
    }

    //-----end-init-----


    public boolean interactWithBucket(ItemStack stack, Level world, BlockPos pos, @Nullable Player
            player, InteractionHand hand) {
        Item item = stack.getItem();

        ItemStack returnStack = ItemStack.EMPTY;
        //fill
        if (this.isEmpty()) {
            if (BucketHelper.isFishBucket(item)) {
                world.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                returnStack = new ItemStack(Items.BUCKET);
                var type = BucketHelper.getEntityTypeFromBucket(stack.getItem());
                var cap = CapturedMobHandler.getDataCap(type, true);
                var f = cap.shouldRenderWithFluid();
                ResourceLocation fluidId = f.map(Utils::getID).orElse(null);
                MobNBTData data = new MobNBTData.Bucket(null, stack.copy(), cap.getFishTextureIndex(), fluidId);
                this.setData(data);
            }
        } else if(item == Items.BUCKET){
            //empty
            if(this.data instanceof MobNBTData.Bucket bucketData){
                world.playSound(null, pos, SoundEvents.BUCKET_FILL_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                returnStack = bucketData.filledBucket.copy();
                this.setData(null);
            }else if(this.data instanceof MobNBTData.Entity && mobInstance != null){
                Entity temp = mobInstance.getEntityForRenderer();
                if(temp != null){
                    ItemStack bucket = BucketHelper.getBucketFromEntity(temp);
                    world.playSound(null, pos, SoundEvents.BUCKET_FILL_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    returnStack = bucket.copy();
                    this.setData(null);
                }
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


    public void tick(Level pLevel, BlockPos pPos) {
        if (this.needsInitialization) this.initializeEntity(pLevel, pPos);
        if (this.mobInstance != null && this.data instanceof MobNBTData.Entity entityData) {
            this.mobInstance.containerTick(pLevel, pPos, entityData.scale, entityData.mobTag);
        }
    }

    public InteractionResult onInteract(Level world, BlockPos pos, Player player, InteractionHand hand) {
        if (this.mobInstance != null && this.data instanceof MobNBTData.Entity entityData) {
            return mobInstance.onPlayerInteract(world, pos, player, hand, entityData.mobTag);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public MobNBTData getData() {
        return data;
    }

    @Nullable
    public Entity getDisplayedMob() {
        if (this.mobInstance != null) {
            return this.mobInstance.getEntityForRenderer();
        }
        return null;
    }

    public Optional<SoftFluid> shouldRenderWithFluid() {
        if (data == null || !this.isAquarium || this.mobProperties == null) return Optional.empty();
        return this.mobProperties.shouldRenderWithFluid();
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
    public static CompoundTag createMobHolderItemTag(Entity mob, float blockW, float blockH, ItemStack bucketStack,
                                                     boolean isAquarium) {
        MobNBTData data;
        String name = mob.getName().getString();
        var cap = CapturedMobHandler.getCatchableMobCapOrDefault(mob);
        if (isAquarium && !bucketStack.isEmpty() && cap.renderAs2DFish()) {
            var f = cap.shouldRenderWithFluid();
            ResourceLocation fluidId = f.map(Utils::getID).orElse(null);
            data = new MobNBTData.Bucket(name,bucketStack, cap.getFishTextureIndex(), fluidId);
        } else {
            Pair<Float, Float> dimensions = calculateMobDimensionsForContainer(mob, blockW, blockH, false);
            float scale = dimensions.getLeft();
            float yOffset = dimensions.getRight();

            CompoundTag mobTag = prepareMobTagForContainer(mob, yOffset);
            if (mobTag == null) return null;
            UUID id = mob.getUUID();
            data = new MobNBTData.Entity(name, mobTag, scale, id);
        }

        CompoundTag cmp = new CompoundTag();
        data.save(cmp, isAquarium);
        return cmp;
    }

    /**
     * prepares the entity nbt to be stored in a MobHolder (Item and Block)
     *
     * @param entity entity
     * @return entity tag
     */
    @Nullable
    private static CompoundTag prepareMobTagForContainer(Entity entity, double yOffset) {
        //set post relative to center block cage
        double px = 0.5;
        double py = yOffset + 0.0001;//+ 0.0625;
        double pz = 0.5;
        entity.setPos(px, py, pz);
        //entity.setMotion(0,0,0);
        entity.xOld = px;
        entity.yOld = py;
        entity.zOld = pz;

        if (entity.isPassenger()) {
            entity.getVehicle().ejectPassengers();
        }
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (entity instanceof Bucketable bucketable) {
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

        if (entity instanceof Bat bat) {
            bat.setResting(true);
        }
        if (entity instanceof Fox fox) {
            fox.setSleeping(true);
        }
        if (entity instanceof AbstractFish abstractFish) {
            abstractFish.setFromBucket(true);
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
     * @param blockW container width
     * @param blockH container height
     * @return scale and y offset
     */
    public static Pair<Float, Float> calculateMobDimensionsForContainer(
            Entity mob, float blockW, float blockH, boolean waterlogged) {

        var cap = CapturedMobHandler.getCatchableMobCapOrDefault(mob);
        float babyScale = 1;

        if (mob instanceof LivingEntity && ((LivingEntity) mob).isBaby()) {
            if ((mob instanceof Villager)) babyScale = 1.125f;
            else if (mob instanceof AgeableMob) babyScale = 2f;
            else babyScale = 1.125f;
        }

        float scale = 1;

        float w = mob.getBbWidth() * babyScale;
        float h = mob.getBbHeight() * babyScale;

        boolean isAir = cap.shouldHover(mob, waterlogged);

        float aW = w + cap.getHitBoxWidthIncrement(mob);
        float aH = h + cap.getHitBoxHeightIncrement(mob);

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
        String name = Utils.getID(mob.getType()).toString();
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
        this.setData(null);
    }

    //what is serialized in an itemStack or container
    //TODO: turn this into a itemStack cap for faster access
    public abstract static class MobNBTData {
        protected final String name;
        protected int fishTexture;
        @Nullable
        protected ResourceLocation fluidID;

        private MobNBTData(String name, int fishTexture, @Nullable ResourceLocation fluidID) {
            this.name = name;
            this.fishTexture = fishTexture;
            this.fluidID = fluidID;
        }

        protected abstract void save(CompoundTag tag, boolean isAquarius);

        @Nullable
        protected static MobNBTData load(CompoundTag tag) {
            if (tag.contains("BucketHolder")) {
               return Bucket.of(tag.getCompound("BucketHolder"));
            } else if (tag.contains("MobHolder")) {
               return Entity.of(tag.getCompound("MobHolder"));
            }
            return null;
        }

        @Nullable
        public String getName() {
            return name;
        }

        public static class Bucket extends MobNBTData {
            private final ItemStack filledBucket;

            protected Bucket(@Nullable String name, ItemStack filledBucket, int fishTexture, @Nullable ResourceLocation fluidId) {
                super(getDefaultName(name, filledBucket), fishTexture, fluidId);
                this.filledBucket = filledBucket;
            }

            private static String getDefaultName(@Nullable String name, @NotNull ItemStack filledBucket) {
                EntityType<?> type;
                if (name == null) {
                    type = BucketHelper.getEntityTypeFromBucket(filledBucket.getItem());
                    name = type == null ? "Mob" : type.getDescriptionId();
                }
                return name;
            }

            @Override
            protected void save(CompoundTag tag, boolean isAquarium) {
                CompoundTag cmp = new CompoundTag();
                cmp.putString("Name", name);
                cmp.put("Bucket", filledBucket.save(new CompoundTag()));
                if (isAquarium) {
                    cmp.putInt("FishTexture", this.fishTexture);
                    if (fluidID != null) {
                        cmp.putString("Fluid", this.fluidID.toString());
                    }
                }
                tag.put("BucketHolder", cmp);
            }

            private static Bucket of(CompoundTag cmp) {
                String name = cmp.getString("Name");
                int fish = cmp.getInt("FishTexture");
                ItemStack bucket = ItemStack.of(cmp.getCompound("Bucket"));
                ResourceLocation fluid = null;
                if (cmp.contains("Fluid")) {
                    fluid = new ResourceLocation(cmp.getString("Fluid"));
                }
                return new Bucket(name, bucket, fish, fluid);
            }
        }

        public static class Entity extends MobNBTData {

            public final CompoundTag mobTag;
            private final float scale;
            @Nullable
            private final UUID uuid;

            protected Entity(String name, CompoundTag tag, float scale, UUID uuid) {
                this(name, 0, null, tag, scale, uuid);
            }

            protected Entity(String name, int fishTexture, @Nullable ResourceLocation fishFluid,
                             CompoundTag tag, float scale, UUID uuid) {
                super(name, fishTexture, fishFluid);
                this.mobTag = tag;
                this.scale = scale;
                this.uuid = uuid;
            }

            private static Entity of(CompoundTag cmp) {
                String name = cmp.getString("Name");
                int fish = cmp.getInt("FishTexture");
                ResourceLocation fluid = null;
                if (cmp.contains("Fluid")) {
                    fluid = new ResourceLocation(cmp.getString("Fluid"));
                }
                CompoundTag entityData = cmp.getCompound("EntityData");
                float scale = cmp.getFloat("Scale");
                UUID uuid = cmp.contains("UUID") ? cmp.getUUID("UUID") : null;
                return new Entity(name, fish, fluid, entityData, scale, uuid);
            }

            public float getScale() {
                return scale;
            }

            @Override
            protected void save(CompoundTag tag, boolean isAquarium) {
                CompoundTag cmp = new CompoundTag();
                cmp.putString("Name", name);
                if (isAquarium) {
                    cmp.putInt("FishTexture", this.fishTexture);
                    if (fluidID != null) {
                        cmp.putString("Fluid", this.fluidID.toString());
                    }
                }
                cmp.put("EntityData", mobTag);
                cmp.putFloat("Scale", scale);
                if (uuid != null) cmp.putUUID("UUID", uuid);
                tag.put("MobHolder", cmp);
            }
        }
    }
}