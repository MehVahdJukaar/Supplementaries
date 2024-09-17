package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Invoker("shouldDropLoot")
    boolean invokeShouldDropLoot();

}
