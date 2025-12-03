package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.entities.goals.PillagerNearestAttackableTargetGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = {"net.minecraft.world.entity.monster.Pillager", "net.minecraft.world.entity.monster.Illusioner", "net.minecraft.world.entity.monster.Evoker", "net.minecraft.world.entity.monster.Vindicator"})
public class PillagerMixin {

    //not compatible unfortunately
    @WrapOperation(method = "registerGoals",
    at = @At(value = "NEW", target = "(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;Z)Lnet/minecraft/world/entity/ai/goal/target/NearestAttackableTargetGoal;"))
    private NearestAttackableTargetGoal<?> supplementaries$increasePillagerTargetingRangeDumb(Mob mob, Class targetType,
                                                                                 boolean mustSee,
                                                                                 Operation<NearestAttackableTargetGoal<?>> original) {
        var originalGoal = original.call(mob, targetType, mustSee);
        return new PillagerNearestAttackableTargetGoal<>(mob, targetType, mustSee);
    }
}
