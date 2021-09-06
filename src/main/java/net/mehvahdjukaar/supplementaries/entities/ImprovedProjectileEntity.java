package net.mehvahdjukaar.supplementaries.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public abstract class ImprovedProjectileEntity extends ProjectileItemEntity {

    public boolean inGround = false;
    public int groundTime = 0;

    protected int maxAge = 200;
    protected int maxGroundTime = 20;
    protected float waterDeceleration = 0.8f;

    protected ImprovedProjectileEntity(EntityType<? extends ProjectileItemEntity> type, World world) {
        super(type, world);
    }

    protected ImprovedProjectileEntity(EntityType<? extends ProjectileItemEntity> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPos(x, y, z);
    }

    protected ImprovedProjectileEntity(EntityType<? extends ProjectileItemEntity> type, LivingEntity thrower, World world) {
        this(type, thrower.getX(), thrower.getEyeY() - (double) 0.1F, thrower.getZ(), world);
        this.setOwner(thrower);
    }

    @Override
    public void tick() {
        //base tick stuff
        if (!this.level.isClientSide) {
            this.setSharedFlag(6, this.isGlowing());
        }
        this.baseTick();


        //fixed vanilla arrow code. You're welcome
        Vector3d movement = this.getDeltaMovement();

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

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        //sets on ground
        if (!blockstate.isAir(this.level, blockpos)) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Vector3d vector3d1 = this.position();

                for (AxisAlignedBB axisalignedbb : voxelshape.toAabbs()) {
                    if (axisalignedbb.move(blockpos).contains(vector3d1)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        if (!this.inGround) {
            this.groundTime = 0;

            this.updateRotation();

            Vector3d pos = this.position();
            boolean client = this.level.isClientSide;

            Vector3d newPos = pos.add(movement);
            RayTraceResult raytraceresult = this.level.clip(new RayTraceContext(pos, newPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                //get correct land pos
                newPos = raytraceresult.getLocation();
            }

            if(client){
                this.spawnTrailParticles(pos, newPos);
            }

            double posX = newPos.x;
            double posY = newPos.y;
            double posZ = newPos.z;

            float deceleration = 0.99F;

            if (this.isInWater()) {
                if (client) {
                    for (int j = 0; j < 4; ++j) {
                        this.level.addParticle(ParticleTypes.BUBBLE, posX - velX * 0.25D, posY - velY * 0.25D, posZ - velZ * 0.25D, velX, velY, velZ);
                    }
                }
                deceleration = this.waterDeceleration;
            }

            this.setDeltaMovement(movement.scale(deceleration));

            if (!this.isNoGravity()) {
                this.setDeltaMovement(velX, velY - this.getGravity(), velZ);
            }
            //first sets correct position, then call hit
            this.setPos(posX, posY, posZ);
            this.checkInsideBlocks();

            //calls on hit
            if (!this.removed) {
                //try hit entity
                EntityRayTraceResult entityraytraceresult = this.findHitEntity(pos, newPos);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null) {
                    RayTraceResult.Type type = raytraceresult.getType();
                    boolean portalHit = false;
                    if (type == RayTraceResult.Type.ENTITY) {
                        Entity entity = ((EntityRayTraceResult) raytraceresult).getEntity();
                        Entity entity1 = this.getOwner();
                        if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity) entity1).canHarmPlayer((PlayerEntity) entity)) {
                            raytraceresult = null;
                        }
                    } else if (type == RayTraceResult.Type.BLOCK) {
                        //portals. done here and not in onBlockHit to prevent any further calls
                        BlockPos hitPos = ((BlockRayTraceResult) raytraceresult).getBlockPos();
                        BlockState hitState = this.level.getBlockState(hitPos);

                        if (hitState.is(Blocks.NETHER_PORTAL)) {
                            this.handleInsidePortal(hitPos);
                            portalHit = true;
                        } else if (hitState.is(Blocks.END_GATEWAY)) {
                            TileEntity tileentity = this.level.getBlockEntity(hitPos);
                            if (tileentity instanceof EndGatewayTileEntity && EndGatewayTileEntity.canEntityTeleport(this)) {
                                ((EndGatewayTileEntity) tileentity).teleportEntity(this);
                            }
                            portalHit = true;
                        }
                    }

                    if (!portalHit && type != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                        this.onHit(raytraceresult);
                        this.hasImpulse = true;
                    }
                }
            }
        } else {
            ++this.groundTime;
        }
        if (!this.level.isClientSide) {
            if (this.tickCount > this.maxAge || this.groundTime > maxGroundTime) {
                this.doStuffBeforeRemoving();
                this.remove();
            }
        }
    }

    public void doStuffBeforeRemoving(){};

    @Nullable
    protected EntityRayTraceResult findHitEntity(Vector3d oPos, Vector3d pos) {
        return ProjectileHelper.getEntityHitResult(this.level, this, oPos, pos, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    public void spawnTrailParticles(Vector3d currentPos, Vector3d newPos){}

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("inGround", this.inGround);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        this.inGround = tag.getBoolean("inGround");
    }

}
