package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.mehvahdjukaar.supplementaries.world.explosion.BombExplosion;
import net.minecraft.block.BlockState;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Random;

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem.class)
public class BombEntity extends ProjectileItemEntity implements IRendersAsItem, IEntityAdditionalSpawnData {
    private double prevX = 0;
    private double prevY = 0;
    private double prevZ = 0;
    private int age = 0;
    private boolean active = true;

    private boolean superCharged = false;

    private int changeTimer = -1;

    private boolean blue = false;

    public BombEntity(EntityType<? extends BombEntity> type, World world) {
        super(type, world);
        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
    }

    public BombEntity(World worldIn, LivingEntity throwerIn, boolean blue) {
        super(Registry.BOMB.get(), throwerIn, worldIn);
        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        this.blue = blue;
    }

    public BombEntity(World worldIn, double x, double y, double z) {
        super(Registry.BOMB.get(), x, y, z, worldIn);
    }

    public BombEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.BOMB.get(), world);
        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Active", this.active);
        compound.putInt("Age", this.age);
        compound.putBoolean("Blue", this.blue);
        compound.putInt("Timer", this.changeTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.active = compound.getBoolean("Active");
        this.age = compound.getInt("Age");
        this.blue = compound.getBoolean("Blue");
        this.changeTimer = compound.getInt("Timer");
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.blue = buffer.readBoolean();
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeBoolean(this.blue);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        return Registry.BOMB_ITEM_ON.get();
    }

    @Override
    public ItemStack getItem() {
        return this.blue ? new ItemStack(this.active ? Registry.BOMB_BLUE_ITEM_ON.get() : Registry.BOMB_BLUE_ITEM.get())
                : new ItemStack(this.active ? Registry.BOMB_ITEM_ON.get() : Registry.BOMB_ITEM.get());
    }

    private void spawnBreakParticles() {
        for (int i = 0; i < 8; ++i) {
            this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(Registry.BOMB_ITEM.get())), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
        default:
            super.handleEntityEvent(id);
            break;
        case 3:
            spawnBreakParticles();
            break;
        case 10:
            spawnBreakParticles();
            level.addParticle(Registry.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), this.getX(), this.getY() + 1, this.getZ(), this.blue ? 5.25D : ServerConfigs.cached.BOMB_RADIUS, 0, 0);
            if(blue){
                for(float d22 = 0; d22 < (Math.PI * 2D); d22 += 0.15707963267948966F) {
                    Vector3d v = new Vector3d(0.55,0,0);
                    v = v.yRot(d22+random.nextFloat()*0.3f);
                    v = v.zRot((float) ((random.nextFloat())* Math.PI));
                    this.level.addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 1, this.getZ(), v.x, v.y, v.z);
                    //this.level.addParticle(ParticleTypes.SPIT, x, y, z, Math.cos(d22) * -10.0D, 0.0D, Math.sin(d22) * -10.0D);
                }
            }

            break;
        case 68:
            level.addParticle(ParticleTypes.FLASH, this.getX(), this.getY() + 1, this.getZ(), 0, 0, 0);
            break;
        case 67:
            Random random = level.getRandom();
            for (int i = 0; i < 10; ++i) {
                level.addParticle(ParticleTypes.SMOKE, this.getX() + 0.25f - random.nextFloat() * 0.5f, this.getY() + 0.45f - random.nextFloat() * 0.5f, this.getZ() + 0.25f - random.nextFloat() * 0.5f, 0, 0.005, 0);
            }
            this.active = false;
            break;

        }
    }

    public double r() {
        return (random.nextGaussian()) * 0.05;
    }

    //TODO: merge with fixed projectile code. fix smoke
    @Override
    public void tick() {
        if (this.changeTimer == 0) {
            if (!this.level.isClientSide) {
                this.explodeOrBreak();
            }
            return;
        }

        if (this.changeTimer > 0) {
            this.changeTimer--;
            level.addParticle(ParticleTypes.SMOKE, this.position().x, this.position().y + 0.5, this.position().z, 0.0D, 0.0D, 0.0D);
        }


        if (this.active && this.isInWater() && !this.blue) {
            this.turnOff();
        }
        if (this.level.isClientSide && this.active) {
            if (this.random.nextFloat() < 1 && !this.firstTick) {
                double x = this.getX();
                double y = this.getY();
                double z = this.getZ();
                Vector3d vector3d = this.getDeltaMovement();
                double dx = vector3d.x;
                double dy = vector3d.y;
                double dz = vector3d.z;
                for(int i = 0; i < 4; ++i) {
                    double j = i/4d;
                    this.level.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(), x + dx * j, 0.5 + y + dy * j, z + dz * j, 0, 0.02, 0);
                }
                /*
                double x2 = (x - this.prevX);
                double y2 = (y - this.prevY);
                double z2 = (z - this.prevZ);
                level.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x + r(), y + 0.5 + r(), z + r(), 0, 0.01, 0);
                level.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x + (x2 / 2) + r(), 0.5 + y + (y2 / 2), z + (z2 / 2) + r(), 0, 0., 0);
                level.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x + (x2 / 4) + r(), 0.5 + y + (y2 / 4), z + (z2 / 4) + r(), 0, 0, 0);
                level.addParticle(Registry.BOMB_SMOKE_PARTICLE.get(),
                        x + (x2 * 0.75) + r(), 0.5 + y + (y2 * 0.75), z + (z2 * 0.75) + r(), 0, 0, 0);
                 */
            }
            this.prevX = this.getX();
            this.prevY = this.getY();
            this.prevZ = this.getZ();

        } else {
            this.age++;
            if (this.age >= 200) this.explodeOrBreak();
        }
        super.tick();

    }


    public static boolean canBreakBlock(IBlockReader world, BlockPos pos, BlockState state, float power) {
        switch (ServerConfigs.cached.BOMB_BREAKS){
            default:
            case NONE:return false;
            case ALL:return true;
            case WEAK:
                return state.canBeReplaced(Fluids.WATER) || state.getBlock() instanceof TNTBlock;
        }
    }


    @Override
    protected void onHitEntity(EntityRayTraceResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 1);
        if (hit.getEntity() instanceof FireballEntity) {
            this.superCharged = true;
            hit.getEntity().remove();
        }
    }

    public void turnOff() {
        if (!level.isClientSide()) {
            this.level.broadcastEntityEvent(this, (byte)67);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
        }
        this.active = false;
    }

    @Override
    public void playerTouch(PlayerEntity entityIn) {
        if (!this.level.isClientSide) {
            if (!this.active && entityIn.inventory.add(this.getItemStack())) {
                entityIn.take(this, 1);
                this.remove();
            }
        }
    }

    private ItemStack getItemStack() {
        return new ItemStack(Registry.BOMB_ITEM.get());
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult hit) {
        super.onHitBlock(hit);
        Vector3d vector3d = hit.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vector3d);
        Vector3d vector3d1 = vector3d.normalize().scale(getGravity());
        this.setPosRaw(this.getX() - vector3d1.x, this.getY() - vector3d1.y, this.getZ() - vector3d1.z);
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(RayTraceResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {

            if (this.blue && this.changeTimer == -1) {
                this.changeTimer = 10;
                //this.setDeltaMovement(Vector3d.ZERO);
                this.level.broadcastEntityEvent(this, (byte) 68);
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundCategory.NEUTRAL, 1.5f,1.3f);
            }

            //normal explosion
            if (!this.removed) {
                if (!this.blue || this.superCharged) {
                    this.explodeOrBreak();
                }
            }
        }
    }


    //explode
    public void explodeOrBreak() {
        if (this.active) {
            this.createExplosion();
            //spawn particles
            this.level.broadcastEntityEvent(this, (byte) 10);
        } else {
            this.level.broadcastEntityEvent(this, (byte) 3);
        }
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_BREAK, SoundCategory.NEUTRAL, 1.5F, 1.5f);
        this.remove();
    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
    private void createExplosion() {

        boolean breaks = this.getOwner() instanceof PlayerEntity ||
                ForgeEventFactory.getMobGriefingEvent(this.level, this.getOwner());

        if(this.superCharged) {
            //second explosion when supercharged
            this.level.explode(this, this.getX(), this.getY(), this.getZ(),                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         6f, breaks, breaks ? Explosion.Mode.DESTROY : Explosion.Mode.NONE);
        }

        BombExplosion explosion = new BombExplosion(this.level, this, null, new ExplosionContext() {
            public boolean shouldBlockExplode(Explosion explosion, IBlockReader reader, BlockPos pos, BlockState state, float power) {
                return canBreakBlock(reader, pos, state, power);
            }
        },
        this.getX(), this.getY() + 0.25, this.getZ(), blue ? 5 : ServerConfigs.cached.BOMB_RADIUS, this.blue, breaks ? Explosion.Mode.BREAK : Explosion.Mode.NONE);

        explosion.explode();
        explosion.doFinalizeExplosion();


    }

    public enum breakingMode{
        ALL,
        WEAK,
        NONE
    }

}
