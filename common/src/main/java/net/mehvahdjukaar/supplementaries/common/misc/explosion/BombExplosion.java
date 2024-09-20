package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
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

import java.util.List;


//TODO: dont subclass explosion. use normal class with parameters
public class BombExplosion extends Explosion {

    private final BombEntity.BombType bombType;

    public BombExplosion(Level world, @Nullable Entity entity, double x, double y, double z,
                          BlockInteraction interaction, BombEntity.BombType bombType) {
        super(world, entity, getBombDamageSource(entity),
                new BombExplosionDamageCalculator(bombType),
                x, y, z, (float) bombType.getRadius(), false, interaction,
                ModParticles.BOMB_EXPLOSION_PARTICLE.get(),
                ModParticles.BOMB_EXPLOSION_PARTICLE_EMITTER.get(),
                ModSounds.BOMB_EXPLOSION.getHolder()
        );
        this.bombType = bombType;
    }

    // client factory
    public BombExplosion(Level level, @Nullable Entity source, double x, double y, double z,
                         float radius, List<BlockPos> toBlow, BlockInteraction blockInteraction,
                         BombEntity.BombType bombType) {
        super(level, source, x, y, z,
                radius, toBlow, blockInteraction,
                ModParticles.BOMB_EXPLOSION_PARTICLE.get(),
                ModParticles.BOMB_EXPLOSION_PARTICLE_EMITTER.get(),
                ModSounds.BOMB_EXPLOSION.getHolder());
        this.bombType = bombType;
    }

    private static @NotNull DamageSource getBombDamageSource(@Nullable Entity source) {
        return ModDamageSources.bombExplosion(source, getIndirectSourceEntityInternal(source));
    }

    public BombEntity.BombType bombType() {
        return bombType;
    }

    @Override
    public ObjectArrayList<BlockPos> getToBlow() {
        return (ObjectArrayList<BlockPos>) super.getToBlow();
    }

    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        super.finalizeExplosion(spawnParticles);
        if (level.isClientSide) {
            bombType.spawnExtraParticles(x, y, z, level);
        }
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

