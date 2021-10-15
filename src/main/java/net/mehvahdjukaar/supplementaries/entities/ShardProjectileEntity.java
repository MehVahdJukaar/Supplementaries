package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;


public class ShardProjectileEntity extends ImprovedProjectileEntity{

    private BlockState lastState;

    private Entity ignoreEntity = null;

    public ShardProjectileEntity(EntityType<? extends ShardProjectileEntity> type, Level world) {
        super(type, world);
    }

    public ShardProjectileEntity(Level worldIn, LivingEntity throwerIn, double x, double y, double z, Vec3 movement, @Nullable Entity ignore) {
        super(ModRegistry.AMETHYST_SHARD.get(), x, y, z, worldIn);
        this.setOwner(throwerIn);
        this.setDeltaMovement(movement);
        this.ignoreEntity = ignore;
    }

    public ShardProjectileEntity(FMLPlayMessages.SpawnEntity packet, Level world) {
        super(ModRegistry.AMETHYST_SHARD.get(), world);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.lastState != null) {
            tag.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(tag.getCompound("inBlockState"));
        }
    }


    @Override
    public void spawnTrailParticles(Vec3 currentPos, Vec3 newPos) {

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
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected Item getDefaultItem() {
        return ModRegistry.AMETHYST_SHARD_ITEM.get();
    }

    @Override
    public void handleEntityEvent(byte id) {}


    @Override
    protected void onHitBlock(BlockHitResult hit) {
        this.lastState = this.level.getBlockState(hit.getBlockPos());
        super.onHitBlock(hit);
        Vec3 vector3d = hit.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vector3d);
        Vec3 vector3d1 = vector3d.normalize().scale(getGravity());
        this.setPosRaw(this.getX() - vector3d1.x, this.getY() - vector3d1.y, this.getZ() - vector3d1.z);
        this.touchedGround = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult p_213868_1_) {
        //super.onHitEntity(p_213868_1_);
        Entity entity = p_213868_1_.getEntity();
        int i = 2;
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
