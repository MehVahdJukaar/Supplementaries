package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.IBeeGrowable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//fixes, hopefully, double plant growth. should work with other mods too
@Mixin(targets = {"net.minecraft.world.entity.animal.Bee$BeeGrowCropGoal"})
public abstract class BeeGoalMixin {

    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.setBlockAndUpdate (Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
                    ordinal = 0))
    public boolean tick(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof IBeeGrowable beeGrowable) {
            return beeGrowable.getPollinated(level, pos, state);
        }
        return level.setBlockAndUpdate(pos, state);
    }
}
