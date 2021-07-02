package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.CrackedBellBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BellAttachment;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class CrackedBellBlockTile extends TileEntity implements ITickableTileEntity {
    public CrackedBellBlockTile() {
        super(Registry.CRACKED_BELL_TILE.get());
    }
    private long lastRingTimestamp;
    public int ticks;
    public boolean shaking;
    public Direction clickDirection;
    private List<LivingEntity> nearbyEntities;
    private boolean resonating;
    private int resonationTicks;

    public boolean isOnFloor(){
        return this.getBlockState().getValue(CrackedBellBlock.ATTACHMENT)==BellAttachment.FLOOR;
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
        this.level.playSound(null, this.getBlockPos(), SoundEvents.BELL_RESONATE, SoundCategory.BLOCKS, 1.0F, 1.0F);
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
            AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos)).inflate(48.0D);
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

    private void makeRaidersGlow(World p_222828_1_) {
        if (!p_222828_1_.isClientSide) {
            this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach(this::glow);
        }
    }

    private void showBellParticles(World p_222826_1_) {
        if (p_222826_1_.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            MutableInt mutableint = new MutableInt(16700985);
            int i = (int)this.nearbyEntities.stream().filter((e) -> blockpos.closerThan(e.position(), 48.0D)).count();
            this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach((e) -> {
                float f = 1.0F;
                float f1 = MathHelper.sqrt((e.getX() - (double)blockpos.getX()) * (e.getX() - (double)blockpos.getX()) + (e.getZ() - (double)blockpos.getZ()) * (e.getZ() - (double)blockpos.getZ()));
                double d0 = (double)((float)blockpos.getX() + 0.5F) + (double)(1.0F / f1) * (e.getX() - (double)blockpos.getX());
                double d1 = (double)((float)blockpos.getZ() + 0.5F) + (double)(1.0F / f1) * (e.getZ() - (double)blockpos.getZ());
                int j = MathHelper.clamp((i - 21) / -2, 3, 15);

                for(int k = 0; k < j; ++k) {
                    int l = mutableint.addAndGet(5);
                    double d2 = (double) ColorHelper.PackedColor.red(l) / 255.0D;
                    double d3 = (double)ColorHelper.PackedColor.green(l) / 255.0D;
                    double d4 = (double)ColorHelper.PackedColor.blue(l) / 255.0D;
                    p_222826_1_.addParticle(ParticleTypes.ENTITY_EFFECT, d0, (float)blockpos.getY() + 0.5F, d1, d2, d3, d4);
                }

            });
        }
    }

    private boolean isRaiderWithinRange(LivingEntity entity) {
        return entity.isAlive() && !entity.removed && this.getBlockPos().closerThan(entity.position(), 48.0D) && entity.getType().is(EntityTypeTags.RAIDERS);
    }

    private void glow(LivingEntity entity) {
        entity.addEffect(new EffectInstance(Effects.GLOWING, 60));
    }
}
