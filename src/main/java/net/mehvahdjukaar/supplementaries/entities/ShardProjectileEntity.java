package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;


public class ShardProjectileEntity extends ProjectileItemEntity{

    private BlockState lastState;
    protected boolean inGround;
    protected int inGroundTime = 0;

    private Entity ignoreEntity = null;

    public ShardProjectileEntity(EntityType<? extends ShardProjectileEntity> type, World world) {
        super(type, world);
    }

    public ShardProjectileEntity(World worldIn, LivingEntity throwerIn, double x, double y, double z, Vector3d movement, @Nullable Entity ignore) {
        super(Registry.AMETHYST_SHARD.get(), x, y, z, worldIn);
        this.setOwner(throwerIn);
        this.setDeltaMovement(movement);
        this.ignoreEntity = ignore;
    }

    public ShardProjectileEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.AMETHYST_SHARD.get(), world);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        if (this.lastState != null) {
            tag.put("inBlockState", NBTUtil.writeBlockState(this.lastState));
        }
        tag.putBoolean("inGround", this.inGround);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("inBlockState", 10)) {
            this.lastState = NBTUtil.readBlockState(tag.getCompound("inBlockState"));
        }
        this.inGround = tag.getBoolean("inGround");
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected Item getDefaultItem() {
        return Registry.AMETHYST_SHARD_ITEM.get();
    }

    @Override
    public void handleEntityEvent(byte id) {}


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

        float horizontalVel = MathHelper.sqrt(getHorizontalDistanceSqr(movement));

        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            this.yRot = (float)(MathHelper.atan2(velX, velZ) * (double)(180F / (float)Math.PI));
            this.xRot = (float)(MathHelper.atan2(velY, horizontalVel) * (double)(180F / (float)Math.PI));
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        if (!blockstate.isAir(this.level, blockpos)) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Vector3d vector3d1 = this.position();

                for(AxisAlignedBB axisalignedbb : voxelshape.toAabbs()) {
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

            Vector3d pos = this.position();
            boolean client = this.level.isClientSide;
            if(client){
                for(int i = 0; i < 2; ++i) {
                    double j = i/2d;
                    this.level.addParticle(ParticleTypes.CRIT, pos.x + velX * j, pos.y + velY * j, pos.z + velZ * j, -velX, -velY + 0.2D, -velZ);
                }
            }

            Vector3d newPos = pos.add(movement);
            RayTraceResult raytraceresult = this.level.clip(new RayTraceContext(pos, newPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                newPos = raytraceresult.getLocation();
            }

            double posX = newPos.x;
            double posY = newPos.y;
            double posZ = newPos.z;

            this.yRot = (float)(MathHelper.atan2(velX, velZ) * (double)(180F / (float)Math.PI));
            this.xRot = (float)(MathHelper.atan2(velY, horizontalVel) * (double)(180F / (float)Math.PI));

            this.xRot = lerpRotation(this.xRotO, this.xRot);
            this.yRot = lerpRotation(this.yRotO, this.yRot);

            float deceleration = 0.99F;
            float gravity = this.getGravity();

            if (this.isInWater()) {
                if(client) {
                    for (int j = 0; j < 4; ++j) {
                        this.level.addParticle(ParticleTypes.BUBBLE, posX - velX * 0.25D, posY - velY * 0.25D, posZ - velZ * 0.25D, velX, velY, velZ);
                    }
                }
                deceleration = 0.6F;
            }


            this.setDeltaMovement(movement.scale(deceleration));

            if (!this.isNoGravity()) {
                this.setDeltaMovement(velX, velY - gravity, velZ);
            }
            //first sets correct position, then call hit
            this.setPos(posX, posY, posZ);
            this.checkInsideBlocks();

            if(!this.removed) {
                EntityRayTraceResult entityraytraceresult = this.findHitEntity(pos, newPos);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
                    Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canHarmPlayer((PlayerEntity)entity)) {
                        raytraceresult = null;
                    }
                }

                if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.onHit(raytraceresult);
                    this.hasImpulse = true;
                }
            }
        }
        else{
            ++this.inGroundTime;
        }
        if (!this.level.isClientSide) {
            if (this.tickCount > 180 || this.inGroundTime > 20) this.remove();
        }
    }

    @Nullable
    protected EntityRayTraceResult findHitEntity(Vector3d oPos, Vector3d pos) {
        return ProjectileHelper.getEntityHitResult(this.level, this, oPos, pos, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult hit) {
        this.lastState = this.level.getBlockState(hit.getBlockPos());
        super.onHitBlock(hit);
        Vector3d vector3d = hit.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vector3d);
        Vector3d vector3d1 = vector3d.normalize().scale(getGravity());
        this.setPosRaw(this.getX() - vector3d1.x, this.getY() - vector3d1.y, this.getZ() - vector3d1.z);
        this.inGround = true;
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
        //super.onHitEntity(p_213868_1_);
        Entity entity = p_213868_1_.getEntity();
        int i = 1;
        entity.hurt(DamageSource.thrown(this, this.getOwner()), (float)i);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if(entity == ignoreEntity) return false;
        return super.canHitEntity(entity);
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}
