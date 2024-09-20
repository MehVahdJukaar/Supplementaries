package net.mehvahdjukaar.supplementaries.common.misc.mob_container;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.CapturedMobInstance;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.allay.Allay;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

//TODO: finish
//I swear I changed this stuff from the ground up at least 5 times, and it just keeps getting messier somehow. It just needs to do so much...
public class MobContainer {

    private final float width;
    private final float height;
    private final boolean isAquarium;

    //stuff that actually gets saved
    @Nullable
    private MobContainer.MobData data;
    //static mob instance created from entity data.
    //handles the animations. Also contains a reference to the entity properties and visual entity itself
    @Nullable
    private CapturedMobInstance<?> mobInstance;
    //mob settings. In case of bucket holder these wil only consist of default cap or data ones
    private ICatchableMob mobProperties;
    private boolean needsInitialization = false;

    public MobContainer(float width, float height, boolean isAquarium) {
        this.width = width;
        this.height = height;
        this.isAquarium = isAquarium;
    }

    public MobContainer makeCopy() {
        MobContainer container = new MobContainer(this.width, this.height, this.isAquarium);
        container.setData(this.data);
        return container;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (this.data != null) {
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            //TODO:check if existing tags are preserved
            var mapBuilder = MobData.CODEC.encode(this.data, ops, ops.mapBuilder());
            mapBuilder.build(tag);
        }
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

        ops.getMap(tag).ifSuccess(map ->
                MobData.CODEC.decode(ops, map).ifSuccess(this::setData));
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isAquarium() {
        return isAquarium;
    }

    public void setData(@Nullable MobContainer.MobData data) {
        this.data = data;
        this.mobInstance = null;
        this.needsInitialization = true;
    }

    //----init----

    private void initializeEntity(Level level, BlockPos pos) {
        this.needsInitialization = false;
        if (data != null && level != null && pos != null) {
            if (data instanceof MobData.Bucket bucketData) {
                var type = BucketHelper.getEntityTypeFromBucket(bucketData.filledBucket.getItem());
                this.mobProperties = CapturedMobHandler.getDataCap(type, true);
            } else if (data instanceof MobData.Entity entityData) {
                Entity entity = createStaticMob(entityData, level, pos);
                if (entity != null) {
                    //visual entity stored in this instance
                    this.mobProperties = CapturedMobHandler.getCatchableMobCapOrDefault(entity);
                    this.mobInstance = mobProperties.createCapturedMobInstance(entity, this.width, this.height);
                    this.mobInstance.onContainerWaterlogged(level.getFluidState(pos).getType() != Fluids.EMPTY,
                            this.width, this.height);
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
    public static Entity createStaticMob(MobData.Entity data, @NotNull Level world, BlockPos pos) {
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
                if (stack.isEmpty()) {
                    Supplementaries.LOGGER.error("Bucket error 3: name none, bucket {}", stack);
                }
                MobData data = new MobData.Bucket(Optional.empty(), stack.copy(), cap.getFishTextureIndex(), f);
                this.setData(data);
            }
        } else if (item == Items.BUCKET) {
            //empty
            if (this.data instanceof MobData.Bucket bucketData) {
                world.playSound(null, pos, SoundEvents.BUCKET_FILL_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                returnStack = bucketData.filledBucket.copy();
                this.setData(null);
            } else if (this.data instanceof MobData.Entity && mobInstance != null) {
                Entity temp = mobInstance.getEntityForRenderer();
                if (temp != null) {
                    ItemStack bucket = BucketHelper.getBucketFromEntity(temp);
                    if (!bucket.isEmpty()) {
                        world.playSound(null, pos, SoundEvents.BUCKET_FILL_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                        returnStack = bucket.copy();
                        this.setData(null);
                    }
                }
            }
        }
        if (!returnStack.isEmpty()) {
            if (player != null) {
                player.awardStat(Stats.ITEM_USED.get(item));
                if (!player.isCreative()) { //TODO: creative check in spawp item
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
        if (this.mobInstance != null && this.data instanceof MobData.Entity entityData) {
            this.mobInstance.containerTick(pLevel, pPos, entityData.scale, entityData.mobTag);
        }
    }

    public ItemInteractionResult onInteract(Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (this.mobInstance != null && this.data instanceof MobData.Entity entityData) {
            return mobInstance.onPlayerInteract(world, pos, player, hand, stack, entityData.mobTag);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    public MobContainer.MobData getData() {
        return data;
    }

    @Nullable
    public Entity getDisplayedMob() {
        if (this.mobInstance != null) {
            return this.mobInstance.getEntityForRenderer();
        }
        return null;
    }

    public Optional<Holder<SoftFluid>> shouldRenderWithFluid() {
        if (data == null || !this.isAquarium || this.mobProperties == null) return Optional.empty();
        return this.mobProperties.shouldRenderWithFluid();
    }

    //item stuff

    /**
     * captures an entity in this container
     *
     * @param mob         entity to be captured
     * @param bucketStack optional filled bucket item
     * @return true if success
     */
    @Nullable
    public boolean captureEntity(Entity mob, ItemStack bucketStack) {
        MobData newData;
        String name = mob.getName().getString();
        var cap = CapturedMobHandler.getCatchableMobCapOrDefault(mob);
        if (isAquarium && !bucketStack.isEmpty() && cap.renderAs2DFish()) {
            var f = cap.shouldRenderWithFluid();
            if (bucketStack.isEmpty()) {
                Supplementaries.LOGGER.error("Bucket error 2: name {}, bucket {}", name, bucketStack);
            }
            newData = new MobData.Bucket(Optional.of(name), bucketStack, cap.getFishTextureIndex(), f);
        } else {
            Pair<Float, Float> dimensions = calculateMobDimensionsForContainer(mob, width, height, isAquarium);
            float scale = dimensions.getLeft();
            float yOffset = dimensions.getRight();

            CompoundTag mobTag = prepareMobTagForContainer(mob, yOffset);
            if (mobTag == null) return false;
            UUID id = mob.getUUID();
            newData = new MobData.Entity(name, mobTag, scale, id);
        }

        this.setData(newData);
        return true;
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
        if (entity instanceof Mob mob && !(mob instanceof Allay)) {
            if (entity instanceof Bucketable) mob.setPersistenceRequired();
        }
        if (entity instanceof Bucketable bucketable) {
            bucketable.setFromBucket(true);
        }

        //prepares entity
        if (entity instanceof LivingEntity le) {
            le.yHeadRotO = 0;
            le.yHeadRot = 0;
            le.walkAnimation.setSpeed(0);
            le.walkAnimation.update(-le.walkAnimation.position(), 1);
            le.walkAnimation.setSpeed(0);
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

        if (mob instanceof LivingEntity livingEntity && livingEntity.isBaby()) {
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
            scale *= 0.45f;
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

    // record. Persistent data that's saved
    public abstract static class MobData {

        // recursive so they are both lazy initialized to prevent deadlock
        public static final MapCodec<MobData> CODEC = MapCodec.recursive("mobData", s -> Codec.mapEither(
                Bucket.CODEC.fieldOf("MobHolder"), Entity.CODEC.fieldOf("BucketHolder")).xmap(Either::unwrap,
                e -> e instanceof Bucket ? Either.left((Bucket) e) : Either.right((Entity) e)));

        public static final StreamCodec<RegistryFriendlyByteBuf, MobData> STREAM_CODEC = StreamCodec.recursive(s ->
                ByteBufCodecs.either(
                        Bucket.STREAM_CODEC, Entity.STREAM_CODEC
                ).map(Either::unwrap, e -> e instanceof Bucket b ? Either.left(b) : Either.right((Entity) e)));

        protected final String name;
        protected final int fishTexture;
        @Nullable
        protected final Holder<SoftFluid> visualFluid;

        private MobData(String name, int fishTexture, @Nullable Holder<SoftFluid> fluidID) {
            this.name = name;
            this.fishTexture = fishTexture;
            this.visualFluid = fluidID;
        }

        public boolean is2DFish() {
            return this.fishTexture != 0;
        }

        public int getFishTexture() {
            return fishTexture;
        }

        @Nullable
        public String getName() {
            return name;
        }

        @Nullable
        public Holder<SoftFluid> getVisualFluid() {
            return visualFluid;
        }

        protected static class Bucket extends MobData {

            public static final Codec<Bucket> CODEC = RecordCodecBuilder.<Bucket>create(instance -> instance.group(
                            Codec.STRING.optionalFieldOf("Name").forGetter(b -> Optional.ofNullable(b.name)),
                            ItemStack.CODEC.fieldOf("Bucket").forGetter(b -> b.filledBucket),
                            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("FishTexture", 0).forGetter(Bucket::getFishTexture),
                            SoftFluid.HOLDER_CODEC.optionalFieldOf("Fluid").forGetter(b -> Optional.ofNullable(b.visualFluid))
                    ).apply(instance, Bucket::new))
                    .validate(b -> {
                        if (b.filledBucket.isEmpty()) return DataResult.error(() -> "Bucket item cannot be empty");
                        return DataResult.success(b);
                    });

            public static final StreamCodec<RegistryFriendlyByteBuf, Bucket> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), b -> Optional.ofNullable(b.name),
                    ItemStack.STREAM_CODEC, b -> b.filledBucket,
                    ByteBufCodecs.VAR_INT, Bucket::getFishTexture,
                    ByteBufCodecs.optional(SoftFluid.STREAM_CODEC), b -> Optional.ofNullable(b.visualFluid),
                    Bucket::new
            );

            private final ItemStack filledBucket;

            protected Bucket(Optional<String> name, ItemStack filledBucket, int fishTexture, Optional<Holder<SoftFluid>> fluidId) {
                super(getDefaultName(name, filledBucket), fishTexture, fluidId.orElse(null));
                this.filledBucket = filledBucket;
            }

            private static String getDefaultName(Optional<String> name, ItemStack filledBucket) {
                EntityType<?> type;
                if (name.isEmpty()) {
                    type = BucketHelper.getEntityTypeFromBucket(filledBucket.getItem());
                    return type == null ? "Mob" : type.getDescriptionId();
                }
                return name.get();
            }
        }

        public static class Entity extends MobData {

            public static final Codec<Entity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("Name").forGetter(Entity::getName),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("FishTexture", 0).forGetter(Entity::getFishTexture),
                    SoftFluid.HOLDER_CODEC.optionalFieldOf("Fluid").forGetter(e -> Optional.ofNullable(e.visualFluid)),
                    CompoundTag.CODEC.fieldOf("EntityData").forGetter(e -> e.mobTag),
                    Codec.FLOAT.fieldOf("Scale").forGetter(Entity::getScale),
                    UUIDUtil.CODEC.fieldOf("UUID").forGetter(e -> e.uuid)
            ).apply(instance, Entity::new));

            public static final StreamCodec<RegistryFriendlyByteBuf, Entity> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, Entity::getName,
                    ByteBufCodecs.VAR_INT, Entity::getFishTexture,
                    ByteBufCodecs.optional(SoftFluid.STREAM_CODEC), e -> Optional.ofNullable(e.visualFluid),
                    ByteBufCodecs.COMPOUND_TAG, e -> e.mobTag,
                    ByteBufCodecs.FLOAT, Entity::getScale,
                    UUIDUtil.STREAM_CODEC, e -> e.uuid,
                    Entity::new
            );

            private final CompoundTag mobTag;
            private final float scale;
            @Nullable
            private final UUID uuid;

            protected Entity(String name, CompoundTag tag, float scale, UUID uuid) {
                this(name, 0, Optional.empty(), tag, scale, uuid);
            }

            protected Entity(String name, int fishTexture, Optional<Holder<SoftFluid>> visualFluid,
                             CompoundTag tag, float scale, UUID uuid) {
                super(name, fishTexture, visualFluid.orElse(null));
                this.mobTag = tag;
                this.scale = scale;
                this.uuid = uuid;
            }


            public float getScale() {
                return scale;
            }
        }
    }
}