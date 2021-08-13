package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

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
    protected void onHit(RayTraceResult hit) {
        super.onHit(hit);
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        if(this.level.isClientSide){
            for(int l2 = 0; l2 < 8; ++l2) {
                this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(Registry.AMETHYST_ARROW_ITEM.get())), x, y, z, random.nextGaussian() * 0.1D, random.nextDouble() * 0.15D, random.nextGaussian() * 0.1D);
            }
        }
        else {
            for(float d22 = 0; d22 < (Math.PI * 2D); d22 += Math.PI/3.5F) {
                Vector3d v = new Vector3d(0.45,0,0);
                v = v.yRot(d22+random.nextFloat()*0.2f);
                v = v.zRot(0.2f); //+(random.nextFloat()-0.5f)*1.1f
                ShardProjectileEntity shard = new ShardProjectileEntity(level, (LivingEntity) this.getOwner(),
                        x, y, z, 0*v.x, 0*v.y, 0*v.z);
                level.addFreshEntity(shard);
                //this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(Registry.STICK_BLOCK.get())),
                        //x, y, z, v.x, v.y, v.z);
                //this.level.addParticle(ParticleTypes.SPIT, x, y, z, Math.cos(d22) * -10.0D, 0.0D, Math.sin(d22) * -10.0D);
            }

            this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.1F, 1.9F);
            //this.applySplash(hit.getType() == RayTraceResult.Type.ENTITY ? ((EntityRayTraceResult) hit).getEntity() : null);
        }
        //this.remove();
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
