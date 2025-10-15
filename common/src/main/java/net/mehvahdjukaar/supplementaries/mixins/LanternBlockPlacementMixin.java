package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LanternBlock.class, priority = 1400) //to happen before amendments one
public abstract class LanternBlockPlacementMixin {

    @ModifyReturnValue(method = {"canSurvive"}, at = {@At("RETURN")})
    private boolean supp$hangFromRopes(boolean original, BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(LanternBlock.HANGING) && IRopeConnection.isSupportingCeiling(
                level.getBlockState(pos.above()), pos.above(), level)) {
            return true;
        }
        return original;
    }

}

