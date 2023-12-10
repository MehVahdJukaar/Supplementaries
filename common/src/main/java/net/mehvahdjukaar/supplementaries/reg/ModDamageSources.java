package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ModDamageSources {

    private static final ResourceKey<DamageType> SPIKE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            Supplementaries.res("bamboo_spikes"));
    private static final ResourceKey<DamageType> BOTTLING_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            Supplementaries.res("xp_extracting"));
    private static final ResourceKey<DamageType> BOMB_EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE,
            Supplementaries.res("bomb_explosion"));
    private static final ResourceKey<DamageType> PLAYER_BOMB_EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE,
            Supplementaries.res("bomb_explosion"));

    private static final DataObjectReference<DamageType> spike = new DataObjectReference<>(SPIKE_DAMAGE.location(), Registries.DAMAGE_TYPE);
    private static final DataObjectReference<DamageType> bottling = new DataObjectReference<>(BOTTLING_DAMAGE.location(), Registries.DAMAGE_TYPE);
    private static final DataObjectReference<DamageType> bombExplosion = new DataObjectReference<>(BOMB_EXPLOSION.location(), Registries.DAMAGE_TYPE);
    private static final DataObjectReference<DamageType> playerBombExplosion = new DataObjectReference<>(PLAYER_BOMB_EXPLOSION.location(), Registries.DAMAGE_TYPE);

    //these are data defined now

    public static DamageSource spikePlayer(Player player) {
        return new SpikePlayerDamageSource(spike.getHolder(), player);
    }

    public static DamageSource spike() {
        return new DamageSource(spike.getHolder());
    }

    public static DamageSource bottling() {
        return new DamageSource(bottling.getHolder());
    }


    public static DamageSource bombExplosion(@Nullable Entity entity, @Nullable Entity entity2) {
        return new DamageSource(entity2 != null && entity != null ? playerBombExplosion.getHolder() : bombExplosion.getHolder(), entity, entity2);
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
