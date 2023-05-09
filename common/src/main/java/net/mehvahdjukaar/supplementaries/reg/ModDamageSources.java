package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.RegistryAccess;
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

    private static DamageSource spikeDamage;
    private static DamageSource bottlingDamage;


    public static void reload(RegistryAccess registryAccess){
        var reg = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
        spikeDamage = new DamageSource(reg.getHolderOrThrow(SPIKE_DAMAGE));
        bottlingDamage = new DamageSource(reg.getHolderOrThrow(BOTTLING_DAMAGE));
    }
    //these are data defined now

    public static DamageSource spikePlayer(Player level) {
        return new SpikePlayerDamageSource(spikeDamage);
    }

    public static DamageSource spike() {
        return spikeDamage;
    }

    public static DamageSource bottling() {
        return bottlingDamage;
    }

    public static class SpikePlayerDamageSource extends DamageSource {

        public SpikePlayerDamageSource(String string, Entity entity) {
            super(string, entity);
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
            String string = "death.attack." + this.msgId;
            String string2 = string + ".player";
            return livingEntity2 != null
                    ? Component.translatable(string2, livingEntity.getDisplayName(), livingEntity2.getDisplayName())
                    : Component.translatable(string, livingEntity.getDisplayName());
        }
    }
}
