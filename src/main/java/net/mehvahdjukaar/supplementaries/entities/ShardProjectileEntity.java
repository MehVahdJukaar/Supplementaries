package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;


public class ShardProjectileEntity extends ImprovedProjectileEntity{

    private BlockState lastState;

    private Entity ignoreEntity = null;

    public ShardProjectileEntity(EntityType<? extends ShardProjectileEntity> type, World world) {
        super(type, world);
    }

    public ShardProjectileEntity(World worldIn, LivingEntity throwerIn, double x, double y, double z, Vector3d movement, @Nullable Entity ignore) {
        super(ModRegistry.AMETHYST_SHARD.get(), x, y, z, worldIn);
        this.setOwner(throwerIn);
        this.setDeltaMovement(movement);
        this.ignoreEntity = ignore;
    }

    public ShardProjectileEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(ModRegistry.AMETHYST_SHARD.get(), world);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        if (this.lastState != null) {
            tag.put("inBlockState", NBTUtil.writeBlockState(this.lastState));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("inBlockState", 10)) {
            this.lastState = NBTUtil.readBlockState(tag.getCompound("inBlockState"));
        }
    }


    @Override
    public void spawnTrailParticles(Vector3d currentPos, Vector3d newPos) {

        double x = currentPos.x;
        double y = currentPos.y;
        double z = currentPos.z;
        double dx = newPos.x - x;
        double dy = newPos.y - y;
        double dz = newPos.z - z;
        int s = 2;
        for(int i = 0; i < s; ++i) {
            double j = i/(double)s;
            this.level.addParticle(ParticleTypes.CRIT, x +dx * j, y + dy * j, z + dz * j, -dx, -dy + 0.2D, -dz);
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected Item getDefaultItem() {
        return ModRegistry.AMETHYST_SHARD_ITEM.get();
    }

    @Override
    public void handleEntityEvent(byte id) {}


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
