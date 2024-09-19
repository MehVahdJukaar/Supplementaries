package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ModDamageSources {

    public static final DynamicHolder<DamageType> SPIKE = DynamicHolder.of(Supplementaries.res("bamboo_spikes"), Registries.DAMAGE_TYPE);
    public static final DynamicHolder<DamageType> BOTTLING = DynamicHolder.of(Supplementaries.res("xp_extracting"), Registries.DAMAGE_TYPE);
    public static final DynamicHolder<DamageType> BOMB = DynamicHolder.of(Supplementaries.res("bomb_explosion"), Registries.DAMAGE_TYPE);
    public static final DynamicHolder<DamageType> PLAYER_BOMB = DynamicHolder.of(Supplementaries.res("player_bomb_explosion"), Registries.DAMAGE_TYPE);
    public static final DynamicHolder<DamageType> CANNONBALL = DynamicHolder.of(Supplementaries.res("cannonball"), Registries.DAMAGE_TYPE);
    public static final DynamicHolder<DamageType> PLAYER_CANNONBALL = DynamicHolder.of(Supplementaries.res("player_cannonball"), Registries.DAMAGE_TYPE);
    public static final DynamicHolder<DamageType> SLINGSHOT = DynamicHolder.of(Supplementaries.res("slingshot"), Registries.DAMAGE_TYPE);

    public static DamageSource spikePlayer(Player player) {
        return new SpikePlayerDamageSource(SPIKE, player);
    }

    public static DamageSource spike() {
        return new DamageSource(SPIKE);
    }

    public static DamageSource bottling() {
        return new DamageSource(BOTTLING);
    }

    public static DamageSource bombExplosion(@Nullable Entity projectile, @Nullable Entity shooter) {
        return new DamageSource(shooter != null && projectile != null ? PLAYER_BOMB : BOMB, projectile, shooter);
    }

    //TODO: why a player damage source here?
    public static DamageSource cannonBallExplosion(@Nullable Entity projectile, @Nullable Entity shooter) {
        return new DamageSource(shooter != null && projectile != null ? PLAYER_CANNONBALL : CANNONBALL, projectile, shooter);
    }

    public static DamageSource slingshot(@Nullable Entity projectile, @Nullable Entity shooter) {
        return new DamageSource(SLINGSHOT, projectile, shooter);
    }

    public static class SpikePlayerDamageSource extends DamageSource {

        public SpikePlayerDamageSource(Holder<DamageType> typeHolder, Entity entity) {
            super(typeHolder, entity);
        }

        @Override
        public boolean scalesWithDifficulty() {
            return false;
        }

        @Nullable
        @Override
        public Vec3 getSourcePosition() {
            return null;
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
            LivingEntity livingEntity2 = livingEntity.getKillCredit();
            String string = "death.attack." + this.type().msgId();
            String string2 = string + ".player";
            return livingEntity2 != null
                    ? Component.translatable(string2, livingEntity.getDisplayName(), livingEntity2.getDisplayName())
                    : Component.translatable(string, livingEntity.getDisplayName());
        }
    }
}
