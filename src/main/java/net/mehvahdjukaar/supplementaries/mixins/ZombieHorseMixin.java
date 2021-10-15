package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ZombieHorse.class})
public abstract class ZombieHorseMixin extends AbstractHorse {

    protected ZombieHorseMixin(EntityType<? extends AbstractHorse> p_i48563_1_, Level p_i48563_2_) {
        super(p_i48563_1_, p_i48563_2_);
    }

    @Override
    public boolean rideableUnderWater() {
        return ServerConfigs.cached.ZOMBIE_HORSE_UNDERWATER;
    }
}
