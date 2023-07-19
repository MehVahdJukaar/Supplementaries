package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.goals.ISuppEvoker;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Evoker.class)
public abstract class EvokerMixin extends SpellcasterIllager implements ISuppEvoker {

    @Unique
    private LivingEntity supplementaries$customWololoo;

    protected EvokerMixin(EntityType<? extends SpellcasterIllager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public LivingEntity supplementaries$getCustomWololoo() {
        return supplementaries$customWololoo;
    }

    @Override
    public void supplementaries$setCustomWololoo(LivingEntity customWololoo) {
        this.supplementaries$customWololoo = customWololoo;
    }

    @Override
    public void supplementaries$setSpellCastingTime(int time) {
        this.spellCastingTickCount = time;
    }
}
