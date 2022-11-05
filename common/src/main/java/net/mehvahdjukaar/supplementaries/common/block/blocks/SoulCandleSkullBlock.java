package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class SoulCandleSkullBlock extends CandleSkullBlock{

    private final Supplier<ParticleType<? extends ParticleOptions>> particle;

    public SoulCandleSkullBlock(Properties properties) {
        super(properties);
        this.particle = CompatHandler.BUZZIER_BEES ? CompatObjects.SMALL_SOUL_FLAME : ()->ParticleTypes.SOUL_FIRE_FLAME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!state.getValue(LIT)) {
            return;
        }
        this.getParticleOffsets(state).forEach(vec3 -> addParticlesAndSound(particle.get(), level, vec3.add(blockPos.getX(), blockPos.getY(), blockPos.getZ()), randomSource));
    }

    protected void addParticlesAndSound(ParticleType<?> particle, Level level, Vec3 vec3, RandomSource randomSource) {
        float f = randomSource.nextFloat();
        if (f < 0.3f) {
            level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
            if (f < 0.17f) {
                level.playLocalSound(vec3.x + 0.5, vec3.y + 0.5, vec3.z + 0.5, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0f + randomSource.nextFloat(), randomSource.nextFloat() * 0.7f + 0.3f, false);
            }
        }
        level.addParticle((ParticleOptions) particle, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
    }
}
