package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ZombieHorseEntity.class})
public abstract class ZombieHorseMixin extends AbstractHorseEntity {

    protected ZombieHorseMixin(EntityType<? extends AbstractHorseEntity> p_i48563_1_, World p_i48563_2_) {
        super(p_i48563_1_, p_i48563_2_);
    }

    @Override
    public boolean rideableUnderWater() {
        return ServerConfigs.cached.ZOMBIE_HORSE_UNDERWATER;
    }
}
