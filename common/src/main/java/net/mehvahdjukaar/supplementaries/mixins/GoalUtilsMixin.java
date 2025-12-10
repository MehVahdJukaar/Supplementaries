package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.controllers.BoatNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//why is vanilla always calling from walk node evaluator instead of the actual used one? jank. Just fixing this...
//Will probably allow existing mobs to pathfind better, like frogs to random stroll in water too
@Mixin(GoalUtils.class)
public class GoalUtilsMixin {

    //Actually i cant evne do that because random swim goal RELIES on that fales information. It relies on using the WalkNodeEvaluator instead of the SwimNodeEvaluator
    //I WANT to crash when another mod is doing this as it would be hard incompat otherwise
    @WrapOperation(method = "hasMalus", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/pathfinder/WalkNodeEvaluator;getPathTypeStatic(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/pathfinder/PathType;"))
    private static PathType supp$fixNonInstanceCall_IDontCare(Mob mob, BlockPos pos, Operation<PathType> original) {
        NodeEvaluator evaluator = mob.getNavigation().getNodeEvaluator();
        if(evaluator instanceof BoatNodeEvaluator){
            return evaluator.getPathType(mob, pos);
        }
        return original.call(mob, pos);
    }


    @WrapOperation(method = "hasMalus", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/PathfinderMob;getPathfindingMalus(Lnet/minecraft/world/level/pathfinder/PathType;)F"))
    private static float supp$giveContextToBoatEvaluator(PathfinderMob instance, PathType type, Operation<Float> original) {
        NodeEvaluator evaluator = instance.getNavigation().getNodeEvaluator();
        if (evaluator instanceof BoatNodeEvaluator be) {
            return be.getPathfindingMalus(type, instance); //boat then calls the mob instance one. shifts control from mob to boat
        }
        return original.call(instance, type);
    }
}
