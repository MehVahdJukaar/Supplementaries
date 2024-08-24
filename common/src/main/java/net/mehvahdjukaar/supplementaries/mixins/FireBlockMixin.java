package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FireBlock;getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I"))
    public int supp$igniteLumisene(int odds, @Local(argsOnly = true) ServerLevel level,
                                   @Local BlockPos.MutableBlockPos pos, @Share("isLiquid") LocalBooleanRef isLiquid) {
        if (odds == 0) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof FlammableLiquidBlock) {
                odds = PlatHelper.getFireSpreadSpeed(state, level, pos, Direction.UP);
                isLiquid.set(true);
            }
        }
        return odds;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            ordinal = 1,
            target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean supp$doIgniteLumisene(ServerLevel level, BlockPos pos, BlockState fireState, int i, Operation<Boolean> original,
                                         @Share("isLiquid") LocalBooleanRef isLiquid) {
        if (isLiquid.get()) {
            BlockState targetState = level.getBlockState(pos);
            if (targetState.getBlock() instanceof FlammableLiquidBlock fl) {
                fl.lightUp(null, targetState, pos, level, ILightable.FireSourceType.FIRE_CHANGE);
            }
        }
        return original.call(level, pos, fireState, i);
    }
}
