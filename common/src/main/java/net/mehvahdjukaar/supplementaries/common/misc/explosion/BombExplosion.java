package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.TntBehavior;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.Explosion.getIndirectSourceEntityInternal;


public class BombExplosion {

    // the radius travels as an int, so send it in tenths. Anything under 1 block would round to nothing
    public static final int RADIUS_PACKET_SCALE = 10;
    // what vanilla's finalizeExplosion would use
    private static final float DEFAULT_EXPLOSION_VOLUME = 4;

    public static Explosion createExplosion(Entity source, ServerLevel level, double x, double y, double z,
                                            BombEntity.BombType type, boolean breaksBlocks) {
        Level.ExplosionInteraction interaction = breaksBlocks ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.TRIGGER;

        DamageSource damageSource = getBombDamageSource(source);
        ExplosionDamageCalculator damageCalculator = new BombExplosionDamageCalculator(type);
        double radius = type.getRadius();
        NetworkHelper.sendToAllClientPlayersTrackingEntity(source,
                new ClientBoundParticlePacket(new Vec3(x, y, z), ClientBoundParticlePacket.Kind.BOMB_EXPLOSION,
                        Mth.ceil(radius * RADIUS_PACKET_SCALE)));

        //TODO: finish
        //   ParticleUtil.spawnParticleInASphere(level, x,y,z, ()-> ModParticles.BOMB_CHARGE.get(), 20,1,1,1
        //           );

        // vanilla always blasts the explosion sound at volume 4, which is far too much for the small
        // shards a blue bomb scatters. Mute it there and play our own quieter, higher pop instead
        boolean ownSound = type.explosionVolume() < DEFAULT_EXPLOSION_VOLUME;
        Explosion explosion = level.explode(source, damageSource, damageCalculator, x, y, z,
                (float) radius, false, interaction,
                ModParticles.BOMB_EXPLOSION_PARTICLE.get(),
                ModParticles.BOMB_EXPLOSION_PARTICLE.get(),
                ownSound ? Holder.direct(SoundEvents.EMPTY) : ModSounds.BOMB_EXPLOSION);

        if (ownSound) {
            level.playSound(null, x, y, z, ModSounds.BOMB_EXPLOSION.get(), SoundSource.BLOCKS,
                    type.explosionVolume(), type.explosionPitch(level.getRandom()));
        }
        return explosion;
    }

    private static @NotNull DamageSource getBombDamageSource(Entity source) {
        return ModDamageSources.bombExplosion(source.level(), source, getIndirectSourceEntityInternal(source));
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
                        TntBehavior.explodesWhenExploded(state);
                default -> false;
            };
        }
    }

}

