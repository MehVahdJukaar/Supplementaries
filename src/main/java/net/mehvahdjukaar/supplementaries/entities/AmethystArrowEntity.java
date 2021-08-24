package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AmethystArrowEntity extends AbstractArrowEntity {

    private final int maxSplashDamage = 6;

    public AmethystArrowEntity(EntityType<? extends AmethystArrowEntity> type, World world) {
        super(type, world);
    }

    public AmethystArrowEntity(World worldIn, LivingEntity throwerIn) {
        super(Registry.AMETHYST_ARROW.get(), throwerIn, worldIn);
        setBaseDamage(1);
    }

    public AmethystArrowEntity(World worldIn, double x, double y, double z) {
        super(Registry.AMETHYST_ARROW.get(), x, y, z, worldIn);
        setBaseDamage(1);
    }

    public AmethystArrowEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.AMETHYST_ARROW.get(), world);
        setBaseDamage(1);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
    }

    @Override
    public void setBaseDamage(double p_70239_1_) {
        super.setBaseDamage(p_70239_1_);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void tick() {
        //base tick stuff
        if (!this.level.isClientSide) {
            this.setSharedFlag(6, this.isGlowing());
        }
        this.baseTick();


        //fixed vanilla arrow code. You're welcome
        boolean flag = this.isNoPhysics();
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
        if (!blockstate.isAir(this.level, blockpos) && !flag) {
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

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        if (this.inGround && !flag) {

            if (!this.level.isClientSide) {
                this.tickDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vector3d pos = this.position();
            Vector3d newPos = pos.add(movement);
            RayTraceResult raytraceresult = this.level.clip(new RayTraceContext(pos, newPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                newPos = raytraceresult.getLocation();
            }

            if (this.isCritArrow()) {
                for(int i = 0; i < 4; ++i) {
                    this.level.addParticle(ParticleTypes.CRIT, this.getX() + velX * (double)i / 4.0D, this.getY() + velY * (double)i / 4.0D, this.getZ() + velZ * (double)i / 4.0D, -velX, -velY + 0.2D, -velZ);
                }
            }

            double posX = newPos.x;
            double posY =  newPos.y;
            double posZ =  newPos.z;

            if (flag) {
                this.yRot = (float)(MathHelper.atan2(-velX, -velZ) * (double)(180F / (float)Math.PI));
            } else {
                this.yRot = (float)(MathHelper.atan2(velX, velZ) * (double)(180F / (float)Math.PI));
            }

            this.xRot = (float)(MathHelper.atan2(velY, horizontalVel) * (double)(180F / (float)Math.PI));
            this.xRot = lerpRotation(this.xRotO, this.xRot);
            this.yRot = lerpRotation(this.yRotO, this.yRot);

            float deceleration = 0.99F;
            float gravity = 0.05F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    this.level.addParticle(ParticleTypes.BUBBLE, posX - velX * 0.25D, posY - velY * 0.25D, posZ - velZ * 0.25D, velX, velY, velZ);
                }
                deceleration = this.getWaterInertia();
            }



            this.setDeltaMovement(movement.scale(deceleration));

            if (!this.isNoGravity() && !flag) {
                this.setDeltaMovement(velX, velY - gravity, velZ);
            }
            //first sets correct position, then call hit
            this.setPos(posX, posY, posZ);
            this.checkInsideBlocks();

            while(!this.removed) {
                EntityRayTraceResult entityraytraceresult = this.findHitEntity(pos, newPos);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
                    Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canHarmPlayer((PlayerEntity)entity)) {
                        raytraceresult = null;
                        entityraytraceresult = null;
                    }
                }

                if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.onHit(raytraceresult);
                    this.hasImpulse = true;
                }

                if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                raytraceresult = null;
            }
        }
    }

    @Override
    protected void onHit(RayTraceResult hit) {
        super.onHit(hit);
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        if (this.level.isClientSide){
            for (int l2 = 0; l2 < 8; ++l2) {
                this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(Registry.AMETHYST_ARROW_ITEM.get())), x, y, z, random.nextGaussian() * 0.1D, random.nextDouble() * 0.15D, random.nextGaussian() * 0.1D);
            }
        }
        else {
            List<Vector3d> directions = getShootVectors(this.random,0);
            for (Vector3d vec : directions) {
                Entity target = null;
                Direction dir = Direction.UP;
                if (hit.getType() == RayTraceResult.Type.ENTITY) {
                    target = ((EntityRayTraceResult) hit).getEntity();
                } else if (hit.getType() == RayTraceResult.Type.BLOCK) {
                    dir = ((BlockRayTraceResult)hit).getDirection();
                }
                vec = vec.scale(0.35f);
                vec = this.rotateVector(vec,dir);
                ShardProjectileEntity shard = new ShardProjectileEntity(level, (LivingEntity) this.getOwner(),
                        x+vec.x,y+vec.y+0.25, vec.z+z, vec, target);
                level.addFreshEntity(shard);
            }
            /*
            for(float d22 = 0; d22 < (Math.PI * 2D)-0.01; d22 += 2*Math.PI/9) {
                Vector3d v = new Vector3d(0.4,0,0);

                v = v.yRot(d22+random.nextFloat()*0.2f);

                Entity target = null;
                if (hit.getType() == RayTraceResult.Type.ENTITY) {
                    target = ((EntityRayTraceResult) hit).getEntity();
                }

                ShardProjectileEntity shard = new ShardProjectileEntity(level, (LivingEntity) this.getOwner(),
                        x,y+0.25, z, v.x, 0.2, v.z, target);

                level.addFreshEntity(shard);
                //this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(Registry.STICK_BLOCK.get())),
                        //x, y, z, v.x, v.y, v.z);
                //this.level.addParticle(ParticleTypes.SPIT, x, y, z, Math.cos(d22) * -10.0D, 0.0D, Math.sin(d22) * -10.0D);
            }
             */

            this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.1F, 1.9F);
            //this.applySplash(hit.getType() == RayTraceResult.Type.ENTITY ? ((EntityRayTraceResult) hit).getEntity() : null);
        }
        this.remove();
    }

    public List<Vector3d> getShootVectors(Random random, float uncertainty){
        List<Vector3d> vectors = new ArrayList<>();
        float turnFraction = (1 + MathHelper.sqrt(5))/2;
        int numPoints = 17;
        double fullness = 0.8;
        for (int i = 1; i <= numPoints; i++){
            float dst = i / ((float)numPoints);
            //in degrees cause MathHelper sin are in deg
            float inclination = (random.nextFloat() - 0.5f) * uncertainty
                    + (float) (  Math.acos(1 - fullness * dst));
            float azimuth = (float) ((random.nextFloat() - 0.5f) * uncertainty
                                + (2f*Math.PI) * (random.nextFloat() + (turnFraction * i)));

            double x = Math.sin(inclination) * Math.cos(azimuth);
            double z = Math.sin(inclination) * Math.sin(azimuth);
            double y = Math.cos(inclination);

            Vector3d vec = new Vector3d(x, y, z);

            if(i==1){
                vec = vec.add(0, 1,0);
                vec = vec.scale(0.5);
            }

            vectors.add(vec);
        }
        return vectors;
    }

    private Vector3d rotateVector(Vector3d v, Direction dir){
        switch (dir){
            default:
            case UP:
                return v;
            case DOWN:
                return v.multiply(0d,-1d,0d);
            case NORTH:
                return new Vector3d(v.z,v.x,-v.y);
            case SOUTH:
                return new Vector3d(v.z,v.x,v.y);
            case WEST:
                return new Vector3d(-v.y,v.z,v.x);
            case EAST:
                return new Vector3d(v.y,v.z,v.x);

        }
    }


    private void applySplash(@Nullable Entity target) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        if (!list.isEmpty()) {
            for(LivingEntity livingentity : list) {
                double d0;
                if(target!=null){
                    //arrow position doesn't seem to be precise at all when hitting entities
                    double x =((this.getX()+target.getX())/2f)-livingentity.getX();
                    double y =((this.getY()+target.getY())/2f)-livingentity.getY();
                    double z =((this.getZ()+target.getZ())/2f)-livingentity.getZ();
                    d0 = x*x + y*y + z*z;
                }
                else {
                    d0 = this.distanceToSqr(livingentity);
                }
                if (d0 < 16.0D) {
                    double power = 1.0D - Math.sqrt(d0) / 4.0D;
                    if (livingentity == target) {
                        power = 1.0D;
                    }
                    int damage = (int)(power * (double)(maxSplashDamage) + 0.5D);

                    livingentity.hurt(CommonUtil.AMETHYST_SHARD_DAMAGE,damage);
                }
            }
        }

    }
}
