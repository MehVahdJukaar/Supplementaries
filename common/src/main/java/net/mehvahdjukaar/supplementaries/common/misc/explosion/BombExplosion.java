package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.Explosion.getIndirectSourceEntityInternal;


public class BombExplosion {

    public static Explosion createExplosion(Entity source, ServerLevel level, double x, double y, double z,
                                            BombEntity.BombType type, boolean breaksBlocks) {
        Level.ExplosionInteraction interaction = breaksBlocks ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.TRIGGER;

        DamageSource damageSource = getBombDamageSource(source);
        ExplosionDamageCalculator damageCalculator = new BombExplosionDamageCalculator(type);
        return level.explode(source, damageSource, damageCalculator, x, y, z,
                (float) type.getRadius(), false, interaction,
                ModParticles.BOMB_EXPLOSION_PARTICLE.get(),
                ModParticles.BOMB_EXPLOSION_PARTICLE_EMITTER.get(),
                ModSounds.BOMB_EXPLOSION.getHolder());
    }

    private static @NotNull DamageSource getBombDamageSource(@Nullable Entity source) {
        return ModDamageSources.bombExplosion(source, getIndirectSourceEntityInternal(source));
    }

    private static class BombExplosionDamageCalculator extends ExplosionDamageCalculator {
        private final BombEntity.BombType type;

        public BombExplosionDamageCalculator(BombEntity.BombType type) {
            this.type = type;
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
            return switch (type.breakMode()) {
                case ALL -> true;
                case WEAK -> state.canBeReplaced(Fluids.WATER) ||
                        state.is(ModTags.BOMB_BREAKABLE) ||
                        state.getBlock() instanceof TntBlock;
                default -> false;
            };
        }
    }

}

