package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.selene.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.common.items.ItemsUtil;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.mehvahdjukaar.selene.entities.ImprovedProjectileEntity;

public class SlingshotProjectileEntity extends ImprovedProjectileEntity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(SlingshotProjectileEntity.class, EntityDataSerializers.BYTE);
    //only client
    public int clientSideReturnTridentTickCount;
    private float xRotInc;
    private float yRotInc;
    private float particleCooldown = 0;
    public Lazy<Integer> light = Lazy.of(() -> {
        Item item = this.getItem().getItem();
        if (item instanceof BlockItem) {
            Block b = ((BlockItem) item).getBlock();
            return b.getLightEmission(b.defaultBlockState(), this.level, this.blockPosition());
        }
        return 0;
    });

    public SlingshotProjectileEntity(LivingEntity thrower, Level world, ItemStack item, ItemStack throwerStack) {
        super(ModRegistry.SLINGSHOT_PROJECTILE.get(), thrower, world);
        this.setItem(item);
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(throwerStack));
        this.setNoGravity(EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.STASIS_ENCHANTMENT.get(), throwerStack) != 0);
        this.maxAge = 700;


        this.yRotInc = (this.random.nextBoolean() ? 1 : -1) * (float) (4 * this.random.nextGaussian() + 7);
        this.xRotInc = (this.random.nextBoolean() ? 1 : -1) * (float) (4 * this.random.nextGaussian() + 7);
        this.setXRot(this.random.nextFloat() * 360);
        this.setYRot(this.random.nextFloat() * 360);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public SlingshotProjectileEntity(PlayMessages.SpawnEntity spawnEntity, Level world) {
        this(ModRegistry.SLINGSHOT_PROJECTILE.get(), world);
    }

    //client factory
    public SlingshotProjectileEntity(EntityType<SlingshotProjectileEntity> type, Level world) {
        super(type, world);
        this.maxAge = 500;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte) 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(ID_LOYALTY, tag.getByte("Loyalty"));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Loyalty", this.entityData.get(ID_LOYALTY));
    }

    public void setLoyalty(ItemStack stack) {
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(stack));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STONE;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        if (entityRayTraceResult.getEntity() instanceof EnderMan enderman) {
            Item item = this.getItem().getItem();
            if (item instanceof BlockItem bi) {
                Block block = bi.getBlock();
                if (block.builtInRegistryHolder().is(BlockTags.ENDERMAN_HOLDABLE) || ServerConfigs.cached.UNRESTRICTED_SLINGSHOT) {
                    if (enderman.getCarriedBlock() == null) {
                        enderman.setCarriedBlock(block.defaultBlockState());
                        this.remove(RemovalReason.DISCARDED);
                    }
                }
            }
        }
    }


    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        //can only place when first hits
        if (this.touchedGround) return;
        Entity owner = this.getOwner();
        boolean success = false;
        if (owner instanceof Player player && player.getAbilities().mayBuild ) {

            ItemStack stack = this.getItem();
            Item item = stack.getItem();
            //block override. mimic forge event
            PlayerInteractEvent.RightClickBlock blockPlaceEvent = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, hit.getBlockPos(), hit);
            ItemsOverrideHandler.tryPerformClickedBlockOverride(blockPlaceEvent, stack, true);

            if (blockPlaceEvent.isCanceled() && blockPlaceEvent.getCancellationResult().consumesAction()) {
                success = true;
            }
            if (!success) {
                //null player so sound always plays
                //hackeries because for some god damn reason after 1.17 just using player here does not play the sound 50% of the times
                Player p = CommonUtil.getEntityStand(this, player);

                success = ItemsUtil.place(item,
                        new BlockPlaceContext(this.level, p, InteractionHand.MAIN_HAND, this.getItem(), hit)).consumesAction();

            }
            if (success) {
                this.remove(RemovalReason.DISCARDED);
            }

        }
    }

    @Override
    public void tick() {

        if (this.isNoPhysics()) {
            int i = this.entityData.get(ID_LOYALTY);
            Entity owner = this.getOwner();
            if (i > 0 && this.isAcceptableReturnOwner(owner)) {
                Vec3 vector3d = new Vec3(owner.getX() - this.getX(), owner.getEyeY() - this.getY(), owner.getZ() - this.getZ());
                this.setPosRaw(this.getX(), this.getY() + vector3d.y * 0.015D * (double) i, this.getZ());
                if (this.level.isClientSide) {
                    this.yOld = this.getY();
                }

                double d0 = 0.05D * (double) i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vector3d.normalize().scale(d0)));

                ++this.clientSideReturnTridentTickCount;
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
        if (this.isNoPhysics() || this.touchedGround) {

            boolean success = playerEntity.getAbilities().instabuild || playerEntity.getInventory().add(this.getItem());

            if (!this.level.isClientSide) {
                if (!success) {
                    this.spawnAtLocation(this.getItem(), 0.1f);
                }
            } else {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
            }
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean hasReachedEndOfLife() {
        if (this.isNoGravity() && this.getDeltaMovement().lengthSqr() < 0.005) return true;
        return super.hasReachedEndOfLife() && !this.isNoPhysics();
    }

    @Override
    public void reachedEndOfLife() {
        if (this.entityData.get(ID_LOYALTY) != 0 && this.isAcceptableReturnOwner(this.getOwner())) {
            this.setNoPhysics(true);
            this.groundTime = 0;
        } else {
            this.spawnAtLocation(this.getItem(), 0.1f);
            super.reachedEndOfLife();
        }
    }


    @Override
    protected void updateRotation() {

        if (!this.isNoPhysics()) {
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
    public void spawnTrailParticles(Vec3 currentPos, Vec3 newPos) {
        if (!this.isNoPhysics()) {
            double d = this.getDeltaMovement().length();
            if (this.tickCount > 1 && d * this.tickCount > 1.5) {
                if (this.isNoGravity()) {

                    Vec3 rot = new Vec3(0.325, 0, 0).yRot(this.tickCount * 0.32f);

                    Vec3 movement = this.getDeltaMovement();
                    Vec3 offset = MthUtils.changeBasisN(movement, rot);

                    double px = newPos.x + offset.x;
                    double py = newPos.y + offset.y; //+ this.getBbHeight() / 2d;
                    double pz = newPos.z + offset.z;

                    movement = movement.scale(0.25);
                    this.level.addParticle(ModRegistry.STASIS_PARTICLE.get(), px, py, pz, movement.x, movement.y, movement.z);
                } else {
                    double interval = 4 / (d * 0.95 + 0.05);
                    if (this.particleCooldown > interval) {
                        this.particleCooldown -= interval;
                        double x = currentPos.x;
                        double y = currentPos.y;//+ this.getBbHeight() / 2d;
                        double z = currentPos.z;
                        this.level.addParticle(ModRegistry.SLINGSHOT_PARTICLE.get(), x, y, z, 0, 0.01, 0);
                    }
                }
            }
        }
    }

    @Override
    protected float getDeceleration() {
        return this.isNoGravity() ? ServerConfigs.cached.SLINGSHOT_DECELERATION : super.getDeceleration();
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
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
    public void readSpawnData(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        if (id != -1) {
            this.setOwner(this.level.getEntity(id));
        }
        this.xRotInc = buffer.readFloat();
        this.yRotInc = buffer.readFloat();
        this.setXRot(buffer.readFloat());
        this.setYRot(buffer.readFloat());
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }
}
