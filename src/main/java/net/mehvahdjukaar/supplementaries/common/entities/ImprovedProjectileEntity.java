package net.mehvahdjukaar.supplementaries.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public abstract class ImprovedProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(ImprovedProjectileEntity.class, EntityDataSerializers.BYTE);

    public boolean touchedGround = false;
    public int groundTime = 0;

    protected int maxAge = 200;
    protected int maxGroundTime = 20;
    protected float waterDeceleration = 0.8f;

    protected ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
    }

    protected ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z, Level world) {
        this(type, world);
        this.setPos(x, y, z);
    }

    protected ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, LivingEntity thrower, Level world) {
        this(type, thrower.getX(), thrower.getEyeY() - (double) 0.1F, thrower.getZ(), world);
        this.setOwner(thrower);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_FLAGS, (byte) 0);
    }

    private void setFlag(int id, boolean value) {
        byte b0 = this.entityData.get(ID_FLAGS);
        if (value) {
            this.entityData.set(ID_FLAGS, (byte) (b0 | id));
        } else {
            this.entityData.set(ID_FLAGS, (byte) (b0 & ~id));
        }

    }

    public void setNoPhysics(boolean noPhysics) {
        this.noPhysics = noPhysics;
        this.setFlag(2, noPhysics);
    }

    public boolean isNoPhysics() {
        if (!this.level.isClientSide) {
            return this.noPhysics;
        } else {
            return (this.entityData.get(ID_FLAGS) & 2) != 0;
        }
    }

    @Override
    public void tick() {
        //base tick stuff
        //this.baseTick();

        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner(), this.blockPosition());
            this.hasBeenShot = true;
        }


        //fixed vanilla arrow code. You're welcome
        Vec3 movement = this.getDeltaMovement();

        double velX = movement.x;
        double velY = movement.y;
        double velZ = movement.z;

        /*
        //set initial rot
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float horizontalVel = MathHelper.sqrt(getHorizontalDistanceSqr(movement));
            this.yRot = (float) (MathHelper.atan2(velX, velZ) * (double) (180F / (float) Math.PI));
            this.xRot = (float) (MathHelper.atan2(velY, horizontalVel) * (double) (180F / (float) Math.PI));
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }*/

        boolean noPhysics = this.isNoPhysics();

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        //sets on ground
        if (!blockstate.isAir() && !noPhysics) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 vector3d1 = this.position();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vector3d1)) {
                        this.touchedGround = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }


        if (this.touchedGround && !noPhysics) {
            this.groundTime++;
        } else {
            this.groundTime = 0;

            this.updateRotation();

            Vec3 pos = this.position();
            boolean client = this.level.isClientSide;

            Vec3 newPos = pos.add(movement);

            HitResult blockHitResult = this.level.clip(new ClipContext(pos, newPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                //get correct land pos
                if (!noPhysics) {
                    newPos = blockHitResult.getLocation();
                }
                //no physics clips through blocks
            }

            if (client) {
                this.spawnTrailParticles(pos, newPos);
            }

            double posX = newPos.x;
            double posY = newPos.y;
            double posZ = newPos.z;

            float deceleration = this.getDeceleration();

            if (this.isInWater()) {
                if (client) {
                    for (int j = 0; j < 4; ++j) {
                        double pY = posY + this.getBbHeight() / 2d;
                        this.level.addParticle(ParticleTypes.BUBBLE, posX - velX * 0.25D, pY - velY * 0.25D, posZ - velZ * 0.25D, velX, velY, velZ);
                    }
                }
                deceleration = this.waterDeceleration;
            }

            this.setDeltaMovement(movement.scale(deceleration));

            if (!this.isNoGravity() && !noPhysics) {
                this.setDeltaMovement(velX, velY - this.getGravity(), velZ);
            }
            //first sets correct position, then call hit
            this.setPos(posX, posY, posZ);
            this.checkInsideBlocks();

            //calls on hit
            if (!this.isRemoved()) {
                //try hit entity
                EntityHitResult hitEntity = this.findHitEntity(pos, newPos);
                if (hitEntity != null) {
                    blockHitResult = hitEntity;
                }

                HitResult.Type type = blockHitResult.getType();
                boolean portalHit = false;
                if (type == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult) blockHitResult).getEntity();
                    if (entity instanceof Player p1 && this.getOwner() instanceof Player p2 && !p2.canHarmPlayer(p1)) {
                        blockHitResult = null;
                    }
                } else if (type == HitResult.Type.BLOCK) {
                    //portals. done here and not in onBlockHit to prevent any further calls
                    BlockPos hitPos = ((BlockHitResult) blockHitResult).getBlockPos();
                    BlockState hitState = this.level.getBlockState(hitPos);

                    if (hitState.is(Blocks.NETHER_PORTAL)) {
                        this.handleInsidePortal(hitPos);
                        portalHit = true;
                    } else if (hitState.is(Blocks.END_GATEWAY)) {
                        if (this.level.getBlockEntity(hitPos) instanceof TheEndGatewayBlockEntity tile && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                            TheEndGatewayBlockEntity.teleportEntity(level, hitPos, hitState, this, tile);
                        }
                        portalHit = true;
                    }
                }

                if (!portalHit && blockHitResult != null && type != HitResult.Type.MISS && !noPhysics && !ForgeEventFactory.onProjectileImpact(this, blockHitResult)) {
                    this.onHit(blockHitResult);
                    this.hasImpulse = true;
                }
            }
        }
        if (this.hasReachedEndOfLife()) {
            this.reachedEndOfLife();
        }
    }

    protected float getDeceleration() {
        return 0.99F;
    }


    /**
     * do stuff before removing, then call remove. Called when age reaches max age
     */
    public boolean hasReachedEndOfLife() {
        return this.tickCount > this.maxAge || this.groundTime > maxGroundTime;
    }

    /**
     * remove condition
     */
    public void reachedEndOfLife() {
        this.remove(RemovalReason.DISCARDED);
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 oPos, Vec3 pos) {
        return ProjectileUtil.getEntityHitResult(this.level, this, oPos, pos, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    public void spawnTrailParticles(Vec3 currentPos, Vec3 newPos) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("touchedGround", this.touchedGround);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.touchedGround = tag.getBoolean("touchedGround");
    }

}
