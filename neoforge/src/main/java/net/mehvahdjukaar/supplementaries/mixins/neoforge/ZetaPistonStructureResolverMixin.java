package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.supplementaries.common.utils.ICooperativePiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.piston.ZetaPistonStructureResolver;

import java.util.List;
import java.util.Set;

@Pseudo
@OptionalMixin("org.violetmoon.zeta.piston.ZetaPistonStructureResolver")
@Mixin(ZetaPistonStructureResolver.class)
public abstract class ZetaPistonStructureResolverMixin implements ICooperativePiston {

    @Shadow @Final private BlockPos pistonPos;
    @Shadow @Final private List<BlockPos> myToPush;
    @Shadow @Final private PistonStructureResolver parent;

    // Forward cooperator state to the {@code parent} vanilla resolver as well: when Zeta is
    // globally disabled, {@code resolve()} delegates to {@code parent.resolve()} and vanilla's
    // mixin needs to see the cooperators on the parent instance.
    @Override
    public void supp$setCooperators(Set<BlockPos> cooperators, int pushLimit,
                                    Direction pistonDirection, boolean extending) {
        this.supp$getCoopState().set(cooperators, pushLimit, pistonDirection, extending);
        if (this.parent instanceof ICooperativePiston parentCoop) {
            parentCoop.supp$setCooperators(cooperators, pushLimit, pistonDirection, extending);
        }
    }

    // Gate Zeta's resolve on real cooperation. When Zeta is globally disabled, resolve delegates
    // to parent.resolve(); the parent's vanilla mixin already gated, so skip re-gating here (our
    // myToPush is empty in that case).
    @ModifyReturnValue(method = "resolve", at = @At("RETURN"))
    private boolean supp$gateOnRealCooperation(boolean original) {
        if (!ZetaPistonStructureResolver.GlobalSettings.isEnabled()) return original;
        return this.supp$getCoopState().gateResolve(original, pistonPos, myToPush);
    }

    // Same boundary-extension as on vanilla, applied to Zeta's overridden addBlockLine.
    @WrapOperation(method = "addBlockLine",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"))
    private boolean supp$wrapPistonEqualsCheck(BlockPos candidate, Object pistonPosArg,
                                               Operation<Boolean> original) {
        return this.supp$getCoopState().wrapEqualsCheck(original.call(candidate, pistonPosArg), candidate);
    }

    // Zeta reads its push limit once at the top of addBlockLine via GlobalSettings.getPushLimit().
    // Boost it to the cooperative limit when cooperators are set, leaving the user's
    // Zeta-configured value alone otherwise.
    @ModifyExpressionValue(method = "addBlockLine",
            at = @At(value = "INVOKE",
                    target = "Lorg/violetmoon/zeta/piston/ZetaPistonStructureResolver$GlobalSettings;getPushLimit()I"))
    private int supp$boostPushLimit(int original) {
        return Math.max(original, this.supp$getCoopState().getPushLimit());
    }
}
