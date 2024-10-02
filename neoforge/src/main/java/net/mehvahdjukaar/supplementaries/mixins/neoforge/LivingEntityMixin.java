package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @WrapOperation(method = "travel", at = @At(value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/world/entity/LivingEntity;isInFluidType(Lnet/minecraft/world/level/material/FluidState;)Z"))
    public boolean supp$onTravel(LivingEntity instance, FluidState state, Operation<Boolean> original) {
        boolean or = original.call(instance, state);
        if(or && state.getType() instanceof FiniteFluid ff){
            return ff.shouldSlowDown(state);
        }
        return or;
    }
}
