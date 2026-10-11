package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PignataAnchorBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PignataEntity extends Entity implements Leashable, ContainerEntity {

    private static final EntityDataAccessor<Optional<BlockPos>> DATA_HOOK_POS = SynchedEntityData.defineId(PignataEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Float> DATA_LEASH_LENGTH = SynchedEntityData.defineId(PignataEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> DATA_HIT_IMPULSE = SynchedEntityData.defineId(PignataEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> DATA_HIT_COUNTER = SynchedEntityData.defineId(PignataEntity.class, EntityDataSerializers.INT);

    private static final byte BURST_EVENT = 60;
    private static final byte POP_EVENT = 61;

    private static final float DAMAGE_TO_BURST = 10;
    private static final int MAX_DROP_ITEM_ENTITIES = 32;
    private static final double YANK_OFF_HOOK_SLACK = 1;
    private static final double SNAPPING_LEASH_SLACK = 6;
    private static final double BLOWN_OFF_HOOK_SPEED = 0.6;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(9, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;
    private float damage;
    @Nullable
    private LeashData leashData;

    //Client only
    private float swingX;
    private float swingZ;
    private float swingXO;
    private float swingZO;
    private float swingSpeedX;
    private float swingSpeedZ;

    private Vec3 lastClientMovement = Vec3.ZERO;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private float lerpYRot;

    public PignataEntity(EntityType<? extends PignataEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HOOK_POS, Optional.empty());
        builder.define(DATA_LEASH_LENGTH, 3f);
        builder.define(DATA_HIT_IMPULSE, new Vector3f());
        builder.define(DATA_HIT_COUNTER, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setHookPos(NbtUtils.readBlockPos(tag, "Hook").orElse(null));
        if (tag.contains("LeashLength")) {
            this.entityData.set(DATA_LEASH_LENGTH, tag.getFloat("LeashLength"));
        }
        this.damage = tag.getFloat("Damage");
        readChestVehicleSaveData(tag, registryAccess());
        this.leashData = readLeashData(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        BlockPos hookPos = getHookPos();
        if (hookPos != null) tag.put("Hook", NbtUtils.writeBlockPos(hookPos));
        tag.putFloat("LeashLength", getLeashLength());
        tag.putFloat("Damage", damage);
        addChestVehicleSaveData(tag, registryAccess());
        writeLeashData(tag, leashData);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        //vanilla leaves yRotO at 0 so it would spin into place on the first tick
        this.yRotO = this.getYRot();
    }

    @Nullable
    public BlockPos getHookPos() {
        return this.entityData.get(DATA_HOOK_POS).orElse(null);
    }

    private void setHookPos(@Nullable BlockPos pos) {
        this.entityData.set(DATA_HOOK_POS, Optional.ofNullable(pos));
    }

    public boolean isHooked() {
        return getHookPos() != null;
    }

    public float getLeashLength() {
        return this.entityData.get(DATA_LEASH_LENGTH);
    }

    private Vec3 getHookPoint() {
        return this.position().add(getLeashOffset(1));
    }

    public void hookTo(BlockPos pos) {
        FluidState fluid = level().getFluidState(pos);
        BlockState anchor = ModRegistry.PIGNATA_ANCHOR.get().defaultBlockState()
                .setValue(PignataAnchorBlock.WATERLOGGED, fluid.is(Fluids.WATER) && fluid.isSource());
        level().setBlock(pos, anchor, 3);
        setHookPos(pos);
        snapToHook(pos);
    }

    private void snapToHook(BlockPos pos) {
        this.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void unhook() {
        BlockPos hookPos = getHookPos();
        if (hookPos == null) return;
        setHookPos(null);
        BlockState state = level().getBlockState(hookPos);
        if (state.is(ModRegistry.PIGNATA_ANCHOR.get())) {
            level().setBlock(hookPos, state.getFluidState().createLegacyBlock(), 3);
        }
    }

    //anchor and piston stuff

    @Nullable
    public static PignataEntity findHookedTo(Level level, BlockPos pos) {
        for (PignataEntity pignata : level.getEntitiesOfClass(PignataEntity.class, new AABB(pos).inflate(1))) {
            if (pos.equals(pignata.getHookPos())) return pignata;
        }
        return null;
    }

    public static void onAnchorMoved(Level level, BlockPos from, BlockPos to) {
        PignataEntity pignata = findHookedTo(level, from);
        if (pignata != null) {
            pignata.setHookPos(to);
            pignata.snapToHook(to);
        }
    }

    @Nullable
    private PistonMovingBlockEntity findMovingAnchor(BlockPos hookPos) {
        Level l = level();
        if (l.getBlockEntity(hookPos) instanceof PistonMovingBlockEntity moving && carriesAnchor(moving)) {
            return moving;
        }
        for (Direction dir : Direction.values()) {
            if (l.getBlockEntity(hookPos.relative(dir)) instanceof PistonMovingBlockEntity moving
                    && moving.getMovementDirection() == dir && carriesAnchor(moving)) {
                return moving;
            }
        }
        return null;
    }


    private static boolean carriesAnchor(PistonMovingBlockEntity moving) {
        return moving.getMovedState().is(ModRegistry.PIGNATA_ANCHOR.get());
    }

    private static Vec3 getMovingAnchorPos(PistonMovingBlockEntity moving, float partialTicks) {
        BlockPos pos = moving.getBlockPos();
        return new Vec3(pos.getX() + 0.5 + moving.getXOff(partialTicks),
                pos.getY() + moving.getYOff(partialTicks),
                pos.getZ() + 0.5 + moving.getZOff(partialTicks));
    }

    @Nullable
    public Vec3 getMovingAnchorRenderOffset(float partialTicks) {
        BlockPos hookPos = getHookPos();
        if (hookPos == null) return null;
        PistonMovingBlockEntity moving = findMovingAnchor(hookPos);
        if (moving == null) return null;
        return getMovingAnchorPos(moving, partialTicks).subtract(this.getPosition(partialTicks));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            tickLerp();
            tickSwing();
        } else {
            breakLeashIfKnotFell();
            if (damage > 0 && tickCount % CommonConfigs.Building.PIGNATA_HEAL_INTERVAL.get() == 0) {
                damage = Math.max(0, damage - 2); //one heart
            }
        }
        if (this.isRemoved()) return;
        BlockPos hookPos = getHookPos();
        if (hookPos != null) {
            tickHooked(hookPos);
        } else if (!level().isClientSide) {
            tickFree();
        }
    }


    private void tickHooked(BlockPos hookPos) {
        if (level().getBlockState(hookPos).is(ModRegistry.PIGNATA_ANCHOR.get())) {
            boolean blownOff = getDeltaMovement().lengthSqr() > BLOWN_OFF_HOOK_SPEED * BLOWN_OFF_HOOK_SPEED;
            if (!level().isClientSide && (blownOff || isYankedByLeash())) unhook();
            else snapToHook(hookPos);
            return;
        }
        PistonMovingBlockEntity moving = findMovingAnchor(hookPos);
        if (moving != null) {
            BlockPos destination = moving.getBlockPos();
            if (!level().isClientSide && !destination.equals(hookPos)) {
                setHookPos(destination);
            }
            Vec3 pos = getMovingAnchorPos(moving, 1);
            this.setPos(pos.x, pos.y, pos.z);
            this.setDeltaMovement(Vec3.ZERO);
        } else if (!level().isClientSide) {
            unhook();
        }
    }

    private void tickFree() {
        Entity holder = getLeashHolder();
        Vec3 movement = getDeltaMovement();
        movement = movement.add(0, -getGravity(), 0);
        if (holder != null) {
            movement = movement.add(getLeashConstraintPull(holder, movement));
        }

        boolean wasOnGround = onGround();
        this.setDeltaMovement(movement);
        this.move(MoverType.SELF, movement);

        Vec3 after = getDeltaMovement().scale(isInWater() ? 0.8 : 0.98);
        if (onGround()) {
            float friction = level().getBlockState(getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98f;
            after = after.multiply(friction, 1, friction);
        }
        this.setDeltaMovement(after);

        //fall damage can burst it during move
        if (onGround() && !wasOnGround && holder == null && !this.isRemoved()) {
            land(-movement.y);
        }
        if (isPushable()) pushTouchingEntities();
    }

    //like boats
    private void pushTouchingEntities() {
        AABB touching = getBoundingBox().inflate(0.2, -0.01, 0.2);
        for (Entity entity : level().getEntities(this, touching, EntitySelector.pushableBy(this))) {
            this.push(entity);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    private void land(double landingSpeed) {
        AABB underCenter = new AABB(getX() - 0.1, getY() - 0.1, getZ() - 0.1, getX() + 0.1, getY(), getZ() + 0.1);
        if (level().noCollision(this, underCenter)) {
            burst(null);
            return;
        }
        //just placed ones also land, from a tiny height
        if (landingSpeed > 0.2) {
            wobble(new Vec3(random.nextGaussian() * 0.05, 0, random.nextGaussian() * 0.05), false);
            this.playSound(SoundEvents.WOOL_FALL, 1, 0.9f);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        int fallDamage = Mth.ceil((fallDistance - 3) * multiplier);
        return fallDamage > 0 && this.hurt(source, fallDamage);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRemoved() || this.isInvulnerableTo(source)) return false;
        if (level().isClientSide) return true;

        Vec3 direction = getHitDirection(source);
        float strength = Mth.clamp(0.08f + amount * 0.04f, 0.08f, 0.5f);
        wobble(direction.scale(strength), true);
        if (!isHooked() && !source.is(DamageTypeTags.NO_KNOCKBACK)) {
            this.setDeltaMovement(getDeltaMovement().add(direction.scale(strength)));
            this.hasImpulse = true;
        }
        this.damage += amount;
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
        if (this.damage >= DAMAGE_TO_BURST) {
            burst(source.getEntity());
        } else {
            this.playSound(ModSounds.PIGNATA_HIT.get(), 1, 0.9f + random.nextFloat() * 0.2f);
        }
        return true;
    }

    private Vec3 getHitDirection(DamageSource source) {
        Vec3 direction;
        if (source.getDirectEntity() instanceof Projectile projectile) {
            direction = projectile.getDeltaMovement();
        } else if (source.getSourcePosition() != null) {
            direction = this.position().subtract(source.getSourcePosition());
        } else {
            return Vec3.ZERO;
        }
        Vec3 horizontal = new Vec3(direction.x, 0, direction.z);
        return horizontal.lengthSqr() < 1.0E-6 ? Vec3.ZERO : horizontal.normalize();
    }

    //y = spawn particles or not
    private void wobble(Vec3 push, boolean wasHit) {
        this.entityData.set(DATA_HIT_IMPULSE, new Vector3f((float) push.x, wasHit ? 1 : 0, (float) push.z));
        this.entityData.set(DATA_HIT_COUNTER, this.entityData.get(DATA_HIT_COUNTER) + 1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == DATA_HIT_COUNTER && level().isClientSide && !this.firstTick) {
            Vector3f push = this.entityData.get(DATA_HIT_IMPULSE);
            swingSpeedX += push.x;
            swingSpeedZ += push.z;
            if (push.y > 0) spawnHitParticles(push);
        }
    }

    public void burst(@Nullable Entity cause) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        level().broadcastEntityEvent(this, BURST_EVENT);
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spillEverything(serverLevel, cause);
        }
        this.gameEvent(GameEvent.ENTITY_DIE, cause);
        this.destroy();
    }

    private void spillEverything(ServerLevel serverLevel, @Nullable Entity cause) {
        Vec3 center = this.position().add(0, getBbHeight() / 2, 0);
        List<ItemStack> loot = new ArrayList<>(contents);
        if (lootTable != null) {
            LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(lootTable);
            LootParams.Builder params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, center)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, cause);
            if (cause instanceof ServerPlayer player) {
                params.withLuck(player.getLuck());
                CriteriaTriggers.GENERATE_LOOT.trigger(player, lootTable);
            }
            table.getRandomItems(params.create(LootContextParamSets.CHEST), lootTableSeed, loot::add);
        }
        int singleItems = 0;
        for (ItemStack stack : loot) {
            while (!stack.isEmpty() && singleItems < MAX_DROP_ITEM_ENTITIES) {
                spillItem(stack.split(1), center);
                singleItems++;
            }
            if (!stack.isEmpty()) spillItem(stack, center);
        }
    }

    private void spillItem(ItemStack stack, Vec3 center) {
        Vec3 speed = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(0.3);
        ItemEntity item = new ItemEntity(level(), center.x, center.y, center.z, stack, speed.x, speed.y, speed.z);
        item.setDefaultPickUpDelay();
        level().addFreshEntity(item);
    }

    private void destroy() {
        dropLeash(true, true);
        this.discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && reason.shouldDestroy()) unhook();
        super.remove(reason);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult leashResult = super.interact(player, hand);
        if (leashResult != InteractionResult.PASS) return leashResult;

        ItemStack held = player.getItemInHand(hand);

        if (!held.getItem().canFitInsideContainerItems()) return InteractionResult.PASS;
        if (!level().isClientSide) {
            unpackChestVehicleLootTable(player);
            ItemStack toStuff = player.hasInfiniteMaterials() ? held.copy() : held;
            if (!stuffItemInside(toStuff)) {
                player.displayClientMessage(Component.translatable("message.supplementaries.pignata.full"), true);
                return InteractionResult.FAIL;
            }
            this.playSound(SoundEvents.BUNDLE_INSERT, 1, 0.8f + random.nextFloat() * 0.4f);
            wobble(new Vec3(random.nextGaussian() * 0.05, 0, random.nextGaussian() * 0.05), false);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private boolean stuffItemInside(ItemStack stack) {
        int countBefore = stack.getCount();
        for (int i = 0; i < contents.size() && !stack.isEmpty(); i++) {
            ItemStack slot = contents.get(i);
            if (slot.isEmpty()) {
                contents.set(i, stack.copyAndClear());
            } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
                int moved = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(moved);
                stack.shrink(moved);
            }
        }
        return stack.getCount() != countBefore;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return contents;
    }

    @Override
    public void clearItemStacks() {
        contents.clear();
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getLootTable() {
        return lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    public int getContainerSize() {
        return contents.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        return getChestVehicleItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return removeChestVehicleItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeChestVehicleItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        setChestVehicleItem(slot, stack);
    }

    @Override
    public SlotAccess getSlot(int slot) {
        return getChestVehicleSlot(slot);
    }

    //hoppers can fill it but not empty it
    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return isChestVehicleStillValid(player);
    }

    @Override
    public void clearContent() {
        clearChestVehicleContent();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return null;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModRegistry.PIGNATA.get());
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return isLeashed() && !isHooked();
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return isHooked() ? PushReaction.IGNORE : PushReaction.NORMAL; //anchor gets pushed instead, we just follow it
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpSteps = steps;
    }

    @Override
    public double lerpTargetX() {
        return lerpSteps > 0 ? lerpX : getX();
    }

    @Override
    public double lerpTargetY() {
        return lerpSteps > 0 ? lerpY : getY();
    }

    @Override
    public double lerpTargetZ() {
        return lerpSteps > 0 ? lerpZ : getZ();
    }

    @Override
    public float lerpTargetYRot() {
        return lerpSteps > 0 ? lerpYRot : getYRot();
    }

    private void tickLerp() {
        if (lerpSteps > 0) {
            this.lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, getXRot());
            lerpSteps--;
        }
    }

    public boolean isSwingingFromFeet() {
        return !isHooked() && onGround() && !isLeashTaut();
    }

    private void tickSwing() {
        swingXO = swingX;
        swingZO = swingZ;
        //the body lags behind whatever moves the pivot
        Vec3 movement = this.position().subtract(xo, yo, zo);
        Vec3 acceleration = movement.subtract(lastClientMovement);
        lastClientMovement = movement;

        boolean fromFeet = isSwingingFromFeet();
        float stiffness = fromFeet ? 0.3f : 0.1f;
        float damping = fromFeet ? 0.25f : 0.06f;
        Vec2 target = getRopeTilt();
        swingSpeedX += (target.x - swingX) * stiffness - swingSpeedX * damping - (float) acceleration.x * 1.5f;
        swingSpeedZ += (target.y - swingZ) * stiffness - swingSpeedZ * damping - (float) acceleration.z * 1.5f;
        swingX = Mth.clamp(swingX + swingSpeedX, -1.3f, 1.3f);
        swingZ = Mth.clamp(swingZ + swingSpeedZ, -1.3f, 1.3f);
    }

    //lines the body up with the leahs. fake physics
    private Vec2 getRopeTilt() {
        Entity holder = getLeashHolder();
        if (holder == null || isHooked() || !isLeashTaut()) return Vec2.ZERO;
        Vec3 rope = holder.getRopeHoldPosition(1).subtract(getHookPoint());
        double horizontal = rope.horizontalDistance();
        if (horizontal < 1.0E-4) return Vec2.ZERO;
        float tilt = (float) Math.min(Mth.atan2(horizontal, rope.y), 1.2);
        return new Vec2((float) (-rope.x / horizontal * tilt), (float) (-rope.z / horizontal * tilt));
    }

    public float getSwingX(float partialTicks) {
        return Mth.lerp(partialTicks, swingXO, swingX);
    }

    public float getSwingZ(float partialTicks) {
        return Mth.lerp(partialTicks, swingZO, swingZ);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == BURST_EVENT) {
            spawnBurstParticles();
        } else if (id == POP_EVENT) {
            for (int i = 0; i < 10; i++) {
                level().addParticle(ParticleTypes.POOF, getRandomX(0.6), getRandomY(), getRandomZ(0.6),
                        random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    private ParticleOptions randomConfetti() {
        return random.nextInt(6) == 0 ? ModParticles.STREAMER_PARTICLE.get() : ModParticles.CONFETTI_PARTICLE.get();
    }

    private void spawnBurstParticles() {
        Vec3 center = this.position().add(0, getBbHeight() / 2, 0);
        for (int i = 0; i < 80; i++) {
            Vec3 speed = new Vec3(random.nextGaussian(), random.nextGaussian() + 0.4, random.nextGaussian())
                    .normalize().scale(0.15 + random.nextFloat() * 0.25);
            level().addParticle(randomConfetti(), center.x, center.y, center.z, speed.x, speed.y, speed.z);
        }
        spawnBreakParticles(40, Vec3.ZERO, 0.15);
        level().playLocalSound(getX(), getY(), getZ(), ModSounds.PIGNATA_BURST.get(), SoundSource.NEUTRAL,
                1, 0.9f + random.nextFloat() * 0.2f, false);
    }

    private void spawnHitParticles(Vector3f push) {
        Vec3 center = this.position().add(0, getBbHeight() / 2, 0);
        for (int i = 0; i < 6; i++) {
            level().addParticle(randomConfetti(),
                    center.x + random.nextGaussian() * 0.2, center.y + random.nextGaussian() * 0.2, center.z + random.nextGaussian() * 0.2,
                    push.x * 0.5 + random.nextGaussian() * 0.05, 0.05 + random.nextFloat() * 0.1, push.z * 0.5 + random.nextGaussian() * 0.05);
        }
        spawnBreakParticles(20, new Vec3(push.x * 0.3, 0.1, push.z * 0.3), 0);
    }

    private void spawnBreakParticles(int count, Vec3 dir, double speed) {
        BlockParticleOption crumbs = new BlockParticleOption(ParticleTypes.BLOCK, ModRegistry.PIGNATA_ANCHOR.get().defaultBlockState());
        for (int i = 0; i < count; i++) {
            level().addParticle(crumbs, getRandomX(0.8), getRandomY(), getRandomZ(0.8),
                    dir.x + random.nextGaussian() * speed, dir.y + random.nextGaussian() * speed, dir.z + random.nextGaussian() * speed);
        }
    }

    @Nullable
    @Override
    public LeashData getLeashData() {
        return leashData;
    }

    @Override
    public void setLeashData(@Nullable LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public void setLeashedTo(Entity leashHolder, boolean broadcastPacket) {
        Leashable.super.setLeashedTo(leashHolder, broadcastPacket);
        if (!level().isClientSide) {
            double distance = getHookPoint().distanceTo(leashHolder.getRopeHoldPosition(1));
            this.entityData.set(DATA_LEASH_LENGTH, (float) Mth.clamp(distance, 1.5, 6));
        }
    }

    // own rope physics in tickFree
    @Override
    public boolean handleLeashAtDistance(Entity leashHolder, float distance) {
        return false;
    }

    @Override
    public Vec3 getLeashOffset(float partialTick) {
        return new Vec3(0, getBbHeight(), 0);
    }

    private boolean isYankedByLeash() {
        Entity holder = getLeashHolder();
        return holder != null && getHookPoint().distanceTo(holder.getRopeHoldPosition(1)) > getLeashLength() + YANK_OFF_HOOK_SLACK;
    }

    private boolean isLeashTaut() {
        Entity holder = getLeashHolder();
        return holder != null && getHookPoint().distanceTo(holder.getRopeHoldPosition(1)) > getLeashLength() - 0.3;
    }

    private Vec3 getLeashConstraintPull(Entity holder, Vec3 movement) {
        Vec3 fencePos = holder.getRopeHoldPosition(1);
        Vec3 hookPos = getHookPoint();
        Vec3 movedHookPos = hookPos.add(movement);
        Vec3 fromHolder = movedHookPos.subtract(fencePos);
        double distance = fromHolder.length();
        double length = getLeashLength();
        if (distance > length + SNAPPING_LEASH_SLACK) {
            dropLeash(true, true);
            return Vec3.ZERO;
        }
        if (distance <= length) return Vec3.ZERO;
        this.resetFallDistance();
        Vec3 onRope = fencePos.add(fromHolder.scale(length / distance));
        return onRope.subtract(movedHookPos);
    }

    //knots only check their fence every 100 ticks
    private void breakLeashIfKnotFell() {
        if (getLeashHolder() instanceof LeashFenceKnotEntity knot && knot.isAlive() && !knot.survives()) {
            knot.dropItem(null);
            knot.discard();
            dropLeash(true, true);
        }
    }
}
