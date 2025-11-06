package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.controllers.BoatNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//why is vanilla always calling from walk node evaluator instead of the actual used one? jank. Just fixing this...
//Will probably allow existing mobs to pathfind better, like frogs to random stroll in water too
@Mixin(GoalUtils.class)
public class GoalUtilsMixin {

    //I WANT to crash when another mod is doing this as it would be hard incompat otherwise
    @Redirect(method = "hasMalus", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/WalkNodeEvaluator;getPathTypeStatic(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/pathfinder/PathType;"))
    private static PathType supp$fixNonInstanceCall_IDontCare(Mob mob, BlockPos pos) {
        NodeEvaluator evaluator = mob.getNavigation().getNodeEvaluator();
        return evaluator.getPathType(mob, pos);
    }

    @Redirect(method = "hasMalus", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/PathfinderMob;getPathfindingMalus(Lnet/minecraft/world/level/pathfinder/PathType;)F"))
    private static float supp$fixNonInstanceCall2_IDontCare(PathfinderMob mob, PathType type) {
        NodeEvaluator evaluator = mob.getNavigation().getNodeEvaluator();
        if (evaluator instanceof BoatNodeEvaluator be) {
            return be.getPathfindingMalus(type, mob);
        }
        return mob.getPathfindingMalus(type);
    }
}
