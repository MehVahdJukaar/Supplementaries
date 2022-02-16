package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.IBeeGrowable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//fixes, hopefully, double plant growth. should work with other mods too
@Mixin(targets = {"net.minecraft.entity.passive.BeeEntity$FindPollinationTargetGoal"})
public abstract class BeePollinateGoalMixin {

    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/World.setBlockAndUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
                    ordinal = 0))
    public boolean tick(World level, BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        if (b instanceof IBeeGrowable) {
            return ((IBeeGrowable) b).getPollinated(level, pos, state);
        }
        return level.setBlockAndUpdate(pos, state);
    }
}
