package net.mehvahdjukaar.supplementaries.common.entities;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.entity.IExtraClientSpawnData;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SlingshotProjectileEntity extends ImprovedProjectileEntity implements IExtraClientSpawnData {
    private static final EntityDataAccessor<Byte> LOYALTY = SynchedEntityData.defineId(SlingshotProjectileEntity.class, EntityDataSerializers.BYTE);
    protected int MAX_AGE = 700;

    //these are used on both sides...need to be synced on creation. Could use only clientside tbh
    private float xRotInc;
    private float yRotInc;
    private float particleCooldown = 0;

    private final Supplier<Integer> light = Suppliers.memoize(() -> {
        Item item = this.getItem().getItem();
        if (item instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            return b.defaultBlockState().getLightEmission();
        }
        return 0;
    });

    public SlingshotProjectileEntity(Level world, ItemStack item, ItemStack throwerStack,
                                     @Nullable LivingEntity thrower) {
        this(world, item, throwerStack);
        if (thrower != null) {
            setPos(thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ());
            this.setOwner(thrower);
        }
    }

    public SlingshotProjectileEntity(Level world, ItemStack item, ItemStack throwerStack) {
        super(ModEntities.SLINGSHOT_PROJECTILE.get(), world);
        this.maxAge = MAX_AGE;
        this.setItem(item);

        this.setLoyalty(getLoyaltyFromItem(item));
        this.setNoGravity(EnchantmentHelper.has(throwerStack, ModEnchantments.PROJECTILE_NO_GRAVITY.get()));

        this.yRotInc = (this.random.nextBoolean() ? 1 : -1) * (float) (4 * this.random.nextGaussian() + 7);
        this.xRotInc = (this.random.nextBoolean() ? 1 : -1) * (float) (4 * this.random.nextGaussian() + 7);
        this.setXRot(this.random.nextFloat() * 360);
        this.setYRot(this.random.nextFloat() * 360);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();

        this.maxStuckTime = 0;
    }

    //client factory
    public SlingshotProjectileEntity(EntityType<SlingshotProjectileEntity> type, Level world) {
        super(type, world);
        this.maxAge = MAX_AGE;
        this.maxStuckTime = 0;
    }

    private byte getLoyaltyFromItem(ItemStack itemStack) {
        Level var3 = this.level();
        return var3 instanceof ServerLevel serverLevel
                ? (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, itemStack, this), 0, 127)
                : 0;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return PlatHelper.getEntitySpawnPacket(this, serverEntity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LOYALTY, (byte) 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setLoyalty(tag.getByte("Loyalty"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Loyalty", this.getLoyalty());
    }

    private void setLoyalty(byte loyalty) {
        this.entityData.set(LOYALTY, loyalty);
    }

    public byte getLoyalty() {
        return this.entityData.get(LOYALTY);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STONE;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        ItemStack stack = this.getItem();
        if (!trySplashPotStuff() &&
                entityRayTraceResult.getEntity() instanceof EnderMan enderman) {
            Item item = stack.getItem();
            if (item instanceof BlockItem bi) {
                Block block = bi.getBlock();
                if (block.builtInRegistryHolder().is(BlockTags.ENDERMAN_HOLDABLE) || CommonConfigs.Tools.UNRESTRICTED_SLINGSHOT.get()) {
                    if (enderman.getCarriedBlock() == null) {
                        enderman.setCarriedBlock(block.defaultBlockState());
                        this.remove(RemovalReason.DISCARDED);
                    }
                }
            }
        }

        if (stack.is(ModTags.SLINGSHOT_DAMAGEABLE)) {
            //TODO: finish this
            float speed = (float) this.getDeltaMovement().length();
            double baseDamage = CommonConfigs.Tools.SLINGSHOT_DAMAGEABLE_DAMAGE.get();
            // max damage same as arrows
            int damage = Mth.ceil(Mth.clamp((double) speed * baseDamage, 0.0, 2.147483647E9));
            entityRayTraceResult.getEntity().hurt(ModDamageSources.slingshot(
                    level(), this, this.getOwner()), damage);

            this.kill();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        //can only place when first hits
        Entity owner = this.getOwner();
        boolean shoulKill;
        Level level = level();
        shoulKill = trySplashPotStuff();
        if (!shoulKill && owner instanceof Player player) {
            if (!Utils.mayPerformBlockAction(player, hit.getBlockPos(), getItem())) return;
            if (CompatHandler.FLAN) {
                if (level.isClientSide || !FlanCompat.canPlace(player, hit.getBlockPos())) {
                    return; //hack since we need client interaction aswell
                }
            }
        }
        if (!shoulKill) {

            ItemStack stack = this.getItem();
            Item item = stack.getItem();

            Player player;
            if (owner instanceof Player p) {
                player = p;
            } else {
                //do we even need a player here
                player = FakePlayerManager.getDefault(this, this);
            }

            //block override. mimic forge event that would have called these
            //we cant call all forge events unfortunately otherwise we could ater the world as if player was there and not just place extra blocks
            InteractionResult overrideResult = InteractEventsHandler.onItemUsedOnBlockHP(player, level, stack, InteractionHand.MAIN_HAND, hit);
            if (overrideResult.consumesAction()) {
                shoulKill = true;
            } else {
                overrideResult = InteractEventsHandler.onItemUsedOnBlock(player, level, stack, InteractionHand.MAIN_HAND, hit);
                if (overrideResult.consumesAction()) shoulKill = true;
            }

            if (!shoulKill) {
                //null player so sound always plays
                //hackery because for some god-damn reason after 1.17 just using player here does not play the sound 50% of the times
                Player fakePlayer = FakePlayerManager.getDefault(this, player);

                BlockPlaceContext context = new BlockPlaceContext(level, fakePlayer, InteractionHand.MAIN_HAND, this.getItem(), hit);
                shoulKill = ItemsUtil.place(item,
                        context).consumesAction();
            }
            if (!shoulKill && item instanceof DispensibleContainerItem dc && item.hasCraftingRemainingItem()) {
                Item craftingRemainingItem = stack.getItem().getCraftingRemainingItem();
                SuppPlatformStuff.dispenseContent(dc, stack, hit, level, player);
                dc.checkExtraContent(player, level, stack, hit.getBlockPos());
                if (craftingRemainingItem != null) {
                    this.setItem(craftingRemainingItem.getDefaultInstance());
                } else shoulKill = true;
            }
            this.isStuck = true;
        }
        if (shoulKill) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    //TODO: fix and add particles and stuff
    private boolean trySplashPotStuff() {
        if (this.getOwner() instanceof LivingEntity le) {
            Projectile ent = null;
            Item item = this.getItem().getItem();
            Level level = level();
            if (item instanceof ThrowablePotionItem) {
                var p = new ThrownPotion(level, le);
                p.setPos(this.getX(), this.getY(), this.getZ());
                p.setItem(this.getItem());
                ent = p;
            } else if (item == Items.FIRE_CHARGE) {
                var p = new SmallFireball(level, le, Vec3.ZERO);
                p.setPos(this.getX(), this.getY(), this.getZ());
                p.setItem(this.getItem());
                ent = p;
            } else if (item instanceof SnowballItem) {
                var s = new Snowball(level, le);
                s.setPos(this.getX(), this.getY(), this.getZ());
                s.setItem(this.getItem());
                ent = s;
            } else if (item instanceof BombItem bi) {
                var s = new BombEntity(level, le, bi.getType());
                s.setPos(this.getX(), this.getY(), this.getZ());
                s.setItem(this.getItem());
                ent = s;
            } else if (item instanceof EnderpearlItem) {
                var s = new ThrownEnderpearl(level, le);
                s.setPos(this.getX(), this.getY(), this.getZ());
                s.setItem(this.getItem());
                ent = s;
            }

            if (ent != null) {
                level.addFreshEntity(ent);
                ent.tick();
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        // no physics is turned on when its coming back. Its also saved to nbt so no extra data is required
        if (this.isNoPhysics()) {
            int loyaltyLevel = this.getLoyalty();
            Entity owner = this.getOwner();
            if (loyaltyLevel > 0 && this.isAcceptableReturnOwner(owner)) {
                Vec3 force = new Vec3(owner.getX() - this.getX(), owner.getEyeY() - this.getY(), owner.getZ() - this.getZ());
                this.setPosRaw(this.getX(), this.getY() + force.y * 0.015D * loyaltyLevel, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double d0 = 0.05D * loyaltyLevel;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(force.normalize().scale(d0)));
            }
        }
        super.tick();
    }

    private boolean isAcceptableReturnOwner(Entity owner) {
        if (owner != null && owner.isAlive()) {
            return !(owner instanceof ServerPlayer) || !owner.isSpectator();
        } else {
            return false;
        }
    }

    @Override
    public void playerTouch(Player playerEntity) {
        // Same as AbstractArrow code. we cant take player.take as that is just for arrows an items
        if (!level().isClientSide && this.hasLeftOwner() && (this.isNoPhysics() || this.isStuck)) {
            boolean success = playerEntity.getAbilities().instabuild || playerEntity.
                    getInventory().add(this.getItem());

            Level level = this.level();
            if (!success) {
                this.spawnAtLocation(this.getItem(), 0.1f);
            } else {
                level.playSound(null, playerEntity, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                        0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F);
            }
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean hasReachedEndOfLife() {
        if (this.isNoGravity() && this.getDeltaMovement().lengthSqr() < 0.005) return true;
        return super.hasReachedEndOfLife();
    }

    @Override
    public void reachedEndOfLife() {
        if (this.getLoyalty() != 0 && this.isAcceptableReturnOwner(this.getOwner())) {
            this.setNoPhysics(true);
            this.stuckTime = 0;
        } else {
            this.spawnAtLocation(this.getItem(), 0.1f);
            super.reachedEndOfLife();
        }
    }

    @Override
    protected void updateRotation() {

        if (!this.isNoGravity()) {
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.setXRot(this.getXRot() + xRotInc);
            this.setYRot(this.getYRot() + yRotInc);
            this.particleCooldown++;
        } else {
            super.updateRotation();
        }
    }

    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        if (!this.isNoPhysics()) {
            double speed = this.getDeltaMovement().length();
            if (this.tickCount > 1 && speed * this.tickCount > 1.5) {
                if (this.isNoGravity()) {

                    Vec3 rot = new Vec3(0.325, 0, 0).yRot(this.tickCount * 0.32f);

                    Vec3 movement = this.getDeltaMovement();
                    Vec3 offset = MthUtils.changeBasisN(movement, rot);

                    double px = getX() + offset.x;
                    double py = getEyeY() + offset.y;
                    double pz = getZ() + offset.z;

                    movement = movement.scale(0.25);
                    this.level().addParticle(ModParticles.STASIS_PARTICLE.get(), px, py, pz, movement.x, movement.y, movement.z);
                } else {
                    //TODO: make these properly equally spaced
                    double interval = 3 / (speed * 0.95 + 0.05);
                    if (this.particleCooldown > interval) {
                        this.particleCooldown -= interval;
                        double x = getX();
                        double y = getEyeY();
                        double z = getZ();
                        this.level().addParticle(ModParticles.SLINGSHOT_PARTICLE.get(), x, y, z, 0, 0.01, 0);
                    }
                }
            }
        }
    }

    @Override
    protected float getInertia() {
        return this.isNoGravity() ? (float) (double) CommonConfigs.Tools.SLINGSHOT_DECELERATION.get() : super.getInertia();
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        Entity entity = this.getOwner();
        int id = -1;
        if (entity != null) {
            id = entity.getId();
        }
        buffer.writeInt(id);
        buffer.writeFloat(this.xRotInc);
        buffer.writeFloat(this.yRotInc);
        buffer.writeFloat(this.getXRot());
        buffer.writeFloat(this.getYRot());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        int id = buffer.readInt();
        if (id != -1) {
            this.setOwner(this.level().getEntity(id));
        }
        this.xRotInc = buffer.readFloat();
        this.yRotInc = buffer.readFloat();
        this.setXRot(buffer.readFloat());
        this.setYRot(buffer.readFloat());
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public int getLightEmission() {
        return light.get();
    }
}
