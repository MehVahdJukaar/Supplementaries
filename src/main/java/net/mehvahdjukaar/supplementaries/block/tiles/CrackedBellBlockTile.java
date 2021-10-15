package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.CrackedBellBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.FastColor;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class CrackedBellBlockTile extends BlockEntity implements TickableBlockEntity {
    public CrackedBellBlockTile() {
        super(ModRegistry.CRACKED_BELL_TILE.get());
    }
    private long lastRingTimestamp;
    public int ticks;
    public boolean shaking;
    public Direction clickDirection;
    private List<LivingEntity> nearbyEntities;
    private boolean resonating;
    private int resonationTicks;

    public boolean isOnFloor(){
        return this.getBlockState().getValue(CrackedBellBlock.ATTACHMENT)==BellAttachType.FLOOR;
    }

    @Override
    public boolean triggerEvent(int index, int data) {
        if (index == 1) {
            this.updateEntities();
            this.resonationTicks = 0;
            this.clickDirection = Direction.from3DDataValue(data);
            this.ticks = 0;
            this.shaking = true;
            return true;
        } else {
            return super.triggerEvent(index, data);
        }
    }

    @Override
    public void tick() {
        if (this.shaking) {
            ++this.ticks;
        }

        if (this.ticks >= 50) {
            this.shaking = false;
            this.ticks = 0;
        }

        if (this.ticks >= 5 && this.resonationTicks == 0 && this.areRaidersNearby()) {
            this.resonating = true;
            this.playResonateSound();
        }

        if (this.resonating) {
            if (this.resonationTicks < 40) {
                ++this.resonationTicks;
            } else {
                this.makeRaidersGlow(this.level);
                this.showBellParticles(this.level);
                this.resonating = false;
            }
        }

    }

    private void playResonateSound() {
        this.level.playSound(null, this.getBlockPos(), SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void onHit(Direction direction) {
        BlockPos blockpos = this.getBlockPos();
        this.clickDirection = direction;
        if (this.shaking) {
            this.ticks = 0;
        } else {
            this.shaking = true;
        }

        this.level.blockEvent(blockpos, this.getBlockState().getBlock(), 1, direction.get3DDataValue());
    }

    private void updateEntities() {
        BlockPos blockpos = this.getBlockPos();
        if (this.level.getGameTime() > this.lastRingTimestamp + 60L || this.nearbyEntities == null) {
            this.lastRingTimestamp = this.level.getGameTime();
            AABB axisalignedbb = (new AABB(blockpos)).inflate(48.0D);
            this.nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        }

        if (!this.level.isClientSide) {
            for(LivingEntity livingentity : this.nearbyEntities) {
                if (livingentity.isAlive() && !livingentity.removed && blockpos.closerThan(livingentity.position(), 32.0D)) {
                    livingentity.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, this.level.getGameTime());
                }
            }
        }

    }

    private boolean areRaidersNearby() {
        BlockPos blockpos = this.getBlockPos();

        for(LivingEntity livingentity : this.nearbyEntities) {
            if (livingentity.isAlive() && !livingentity.removed && blockpos.closerThan(livingentity.position(), 32.0D) && livingentity.getType().is(EntityTypeTags.RAIDERS)) {
                return true;
            }
        }

        return false;
    }

    private void makeRaidersGlow(Level p_222828_1_) {
        if (!p_222828_1_.isClientSide) {
            this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach(this::glow);
        }
    }

    private void showBellParticles(Level p_222826_1_) {
        if (p_222826_1_.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            MutableInt mutableint = new MutableInt(16700985);
            int i = (int)this.nearbyEntities.stream().filter((e) -> blockpos.closerThan(e.position(), 48.0D)).count();
            this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach((e) -> {
                float f = 1.0F;
                float f1 = Mth.sqrt((e.getX() - (double)blockpos.getX()) * (e.getX() - (double)blockpos.getX()) + (e.getZ() - (double)blockpos.getZ()) * (e.getZ() - (double)blockpos.getZ()));
                double d0 = (double)((float)blockpos.getX() + 0.5F) + (double)(1.0F / f1) * (e.getX() - (double)blockpos.getX());
                double d1 = (double)((float)blockpos.getZ() + 0.5F) + (double)(1.0F / f1) * (e.getZ() - (double)blockpos.getZ());
                int j = Mth.clamp((i - 21) / -2, 3, 15);

                for(int k = 0; k < j; ++k) {
                    int l = mutableint.addAndGet(5);
                    double d2 = (double) FastColor.ARGB32.red(l) / 255.0D;
                    double d3 = (double)FastColor.ARGB32.green(l) / 255.0D;
                    double d4 = (double)FastColor.ARGB32.blue(l) / 255.0D;
                    p_222826_1_.addParticle(ParticleTypes.ENTITY_EFFECT, d0, (float)blockpos.getY() + 0.5F, d1, d2, d3, d4);
                }

            });
        }
    }

    private boolean isRaiderWithinRange(LivingEntity entity) {
        return entity.isAlive() && !entity.removed && this.getBlockPos().closerThan(entity.position(), 48.0D) && entity.getType().is(EntityTypeTags.RAIDERS);
    }

    private void glow(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
    }
}
