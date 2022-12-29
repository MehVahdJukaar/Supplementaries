package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.world.entity.LivingEntity;

public interface ISuppEvoker {

    void setCustomWololoo(LivingEntity entity);

    LivingEntity getCustomWololoo();

    void setSpellCastingTime(int time);
}
