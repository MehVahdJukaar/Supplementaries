package net.mehvahdjukaar.supplementaries.reg;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ModDamageSources extends DamageSource {

    protected ModDamageSources(String string) {
        super(string);
    }

    public static final DamageSource SPIKE_DAMAGE = new ModDamageSources("supplementaries.bamboo_spikes");
    public static final DamageSource BOTTLING_DAMAGE = new ModDamageSources("supplementaries.xp_extracting");

    public static DamageSource bombExplosion(@Nullable Explosion explosion) {
        return explosion(explosion != null ? explosion.getSourceMob() : null);
    }

    public static DamageSource bombExplosion(@Nullable LivingEntity livingEntity) {
        return livingEntity != null ? (new EntityDamageSource("supplementaries.bomb_explosion.player", livingEntity)).setScalesWithDifficulty().setExplosion() :
                (new ModDamageSources("supplementaries.bomb_explosion")).setScalesWithDifficulty().setExplosion();
    }

    public static class SpikePlayer extends EntityDamageSource {

        public SpikePlayer(String string, Entity entity) {
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
        public String toString() {
            return "DamageSource (" + this.msgId + ")";
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
