package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.supplementaries.common.misc.CoopResolverHelper;
import net.mehvahdjukaar.supplementaries.common.misc.CoopResolverState;
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

/**
 * Apply the same cooperative logic to Quark/Zeta's wrapping resolver. Zeta extends vanilla
 * and inherits the {@code ICooperativePiston} interface + {@code supp$coopState} field from
 * the vanilla mixin, so we only need to re-target the injections at Zeta's own overridden
 * {@code resolve()} and {@code addBlockLine()} (which use {@code myToPush} and Zeta's
 * configurable push limit) and forward {@code setCooperators} to the parent resolver for
 * the Zeta-disabled delegation path.
 */
@Pseudo
@OptionalMixin("org.violetmoon.zeta.piston.ZetaPistonStructureResolver")
@Mixin(ZetaPistonStructureResolver.class)
public abstract class ZetaPistonStructureResolverMixin implements ICooperativePiston {

    @Shadow @Final private BlockPos pistonPos;
    @Shadow @Final private List<BlockPos> myToPush;
    @Shadow @Final private PistonStructureResolver parent;

    /**
     * Forward cooperator state to the {@code parent} vanilla resolver as well: when Zeta is
     * globally disabled, {@code resolve()} delegates to {@code parent.resolve()} and vanilla's
     * mixin needs to see the cooperators on the parent instance.
     */
    @Override
    public void supp$setCooperators(Set<BlockPos> cooperators, int pushLimit,
                                    Direction pistonDirection, boolean extending) {
        this.supp$getCoopState().set(cooperators, pushLimit, pistonDirection, extending);
        if (this.parent instanceof ICooperativePiston parentCoop) {
            parentCoop.supp$setCooperators(cooperators, pushLimit, pistonDirection, extending);
        }
    }

    /**
     * Gate Zeta's resolve on real cooperation. When Zeta is globally disabled, {@code resolve}
     * delegates to {@code parent.resolve()} — the parent's vanilla mixin already gated, so
     * skip re-gating here (our {@code myToPush} is empty in that case).
     */
    @ModifyReturnValue(method = "resolve", at = @At("RETURN"))
    private boolean supp$gateOnRealCooperation(boolean original) {
        if (!ZetaPistonStructureResolver.GlobalSettings.isEnabled()) return original;
        CoopResolverState state = this.supp$getCoopState();
        return CoopResolverHelper.gateResolve(original, pistonPos, myToPush, state);
    }

    /** Same boundary-extension as on vanilla, applied to Zeta's overridden {@code addBlockLine}. */
    @WrapOperation(method = "addBlockLine",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"))
    private boolean supp$wrapPistonEqualsCheck(BlockPos candidate, Object pistonPosArg,
                                               Operation<Boolean> original) {
        return CoopResolverHelper.wrapEqualsCheck(
                original.call(candidate, pistonPosArg), candidate, this.supp$getCoopState());
    }

    /**
     * Zeta reads its push limit once at the top of {@code addBlockLine} via
     * {@code GlobalSettings.getPushLimit()}. Boost it to the cooperative limit when
     * cooperators are set — leaves the user's Zeta-configured value alone otherwise.
     */
    @ModifyExpressionValue(method = "addBlockLine",
            at = @At(value = "INVOKE",
                    target = "Lorg/violetmoon/zeta/piston/ZetaPistonStructureResolver$GlobalSettings;getPushLimit()I"))
    private int supp$boostPushLimit(int original) {
        return Math.max(original, this.supp$getCoopState().pushLimit);
    }
}
