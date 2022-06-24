package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.impl.entities.ImprovedProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

public class ThrowableBrickEntity extends ImprovedProjectileEntity {
    public ThrowableBrickEntity(EntityType<? extends ThrowableBrickEntity> type, Level world) {
        super(type, world);
    }

    public ThrowableBrickEntity(Level worldIn, LivingEntity throwerIn) {
        super(ModRegistry.THROWABLE_BRICK.get(), throwerIn, worldIn);
    }

    public ThrowableBrickEntity(Level worldIn, double x, double y, double z) {
        super(ModRegistry.THROWABLE_BRICK.get(), x, y, z, worldIn);
    }

    public ThrowableBrickEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(ModRegistry.THROWABLE_BRICK.get(), world);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected Item getDefaultItem() {
        return Items.BRICK;
    }


    private ParticleOptions makeParticle() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(this.getDefaultItem())) : new ItemParticleOption(ParticleTypes.ITEM, itemstack);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions iparticledata = this.makeParticle();

            for (int i = 0; i < 8; ++i) {
                this.level.addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult rayTraceResult) {
        super.onHitBlock(rayTraceResult);
        if (!this.level.isClientSide) {
            Entity entity = this.getOwner();
            if (entity instanceof Player && !((Player) entity).mayBuild()) return;
            if (!(entity instanceof Mob) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {

                BlockPos pos = rayTraceResult.getBlockPos();
                if (level.getBlockState(pos).getBlock() instanceof JarBlock) {
                    level.destroyBlock(pos, true);
                } else {
                    breakGlass(pos, 6);
                }
            }
        }
    }

    private static boolean isGlass(BlockState s) {
        try {
            return ((Tags.Blocks.GLASS_PANES != null && s.is(Tags.Blocks.GLASS_PANES))
                    || (Tags.Blocks.GLASS != null && s.is(Tags.Blocks.GLASS)));
        } catch (Exception e) {
            return false;
        }
    }

    private void breakGlass(BlockPos pos, int chance) {
        int c = chance - 1 - this.random.nextInt(4);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock().getExplosionResistance() > 3) return;
        if (c < 0 || !isGlass(state)) return;

        level.destroyBlock(pos, true);
        breakGlass(pos.above(), c);
        breakGlass(pos.below(), c);
        breakGlass(pos.east(), c);
        breakGlass(pos.west(), c);
        breakGlass(pos.north(), c);
        breakGlass(pos.south(), c);

    }


    @Override
    protected void onHitEntity(EntityHitResult p_213868_1_) {
        super.onHitEntity(p_213868_1_);
        Entity entity = p_213868_1_.getEntity();
        int i = 1;
        entity.hurt(DamageSource.thrown(this, this.getOwner()), (float) i);
    }


    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            Vec3 v = result.getLocation();
            this.level.playSound(null, v.x, v.y, v.z, SoundEvents.NETHER_BRICKS_BREAK, SoundSource.NEUTRAL, 0.75F, 1);
            this.level.broadcastEntityEvent(this, (byte) 3);
            this.remove(RemovalReason.DISCARDED);
        }

    }

    @Override
    protected void updateRotation() {
    }
}
