package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.CooperativePistonData;
import net.mehvahdjukaar.supplementaries.common.utils.ICooperativePiston;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    /**
     * Intercepts the extension feasibility resolve in checkIfExtend.
     * <p>
     * Protocol per piston:
     *  1. Register this piston as attempting to extend this tick.
     *  2. Try the vanilla single-piston resolve. If it succeeds, mark posted and let
     *     vanilla post the block event normally.
     *  3. If vanilla fails, look for same-direction pistons that registered earlier this
     *     tick. If none exist, return false as usual.
     *  4. Set cooperators on the SAME resolver and call {@code resolve()} again — both
     *     vanilla and Zeta's resolver clear their toPush at the start of resolve(), so
     *     re-running is safe and avoids allocating a second resolver (which would also
     *     bypass any Quark/Zeta wrapping applied at the original NEW site).
     *  5. Manually post block events for any cooperators whose events weren't posted yet,
     *     then return true so vanilla posts this piston's event.
     */
    @WrapOperation(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"))
    private boolean supp$wrapCheckIfExtendResolve(PistonStructureResolver resolver, Operation<Boolean> original,
                                                  @Local(argsOnly = true) Level level,
                                                  @Local(argsOnly = true) BlockPos pos,
                                                  @Local Direction direction) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return original.call(resolver);
        if (!(level instanceof ServerLevel serverLevel)) return original.call(resolver);

        long tick = level.getGameTime();
        CooperativePistonData data = ModData.COOPERATIVE_PISTONS.getData(serverLevel);
        data.markAttempting(pos, direction, true, tick);
        CooperativePistonData.markAttemptingClient(pos, direction, true, tick);

        boolean vanillaResult = original.call(resolver);
        if (vanillaResult) {
            data.markPosted(pos, tick);
            return true;
        }

        Set<BlockPos> cooperators = data.getCooperators(pos, direction, true, tick);
        if (cooperators.isEmpty()) return false;

        Set<BlockPos> allPistons = new HashSet<>(cooperators);
        allPistons.add(pos);
        int limit = allPistons.size() * 12;

        ICooperativePiston coop = (ICooperativePiston) resolver;
        coop.supp$setCooperators(allPistons, limit, direction, true);
        boolean coopResult = resolver.resolve();
        if (!coopResult) return false;

        // Only post events for cooperators that actually shared the pushed structure.
        // Free-riders are filtered out by the helper's contribution check; their own
        // vanilla resolve already succeeded independently if applicable.
        Set<BlockPos> contributing = coop.supp$getCoopState().contributingCooperators;
        for (BlockPos cooperatorPos : contributing) {
            if (!data.hasPosted(cooperatorPos, tick)) {
                level.blockEvent(cooperatorPos,
                        level.getBlockState(cooperatorPos).getBlock(),
                        0,
                        direction.get3DDataValue());
                data.markPosted(cooperatorPos, tick);
            }
        }
        data.markPosted(pos, tick);
        return true;
    }

    /**
     * Register sticky pistons that are about to retract. Retraction's checkIfExtend never
     * creates a resolver — it just posts a block event with id 1 or 2 — so we hook the
     * blockEvent call itself to register the piston for cooperative pulling.
     */
    @Inject(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V",
                    ordinal = 1))
    private void supp$registerRetraction(Level level, BlockPos pos, BlockState state, CallbackInfo ci,
                                         @Local Direction direction) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        long tick = level.getGameTime();
        ModData.COOPERATIVE_PISTONS.getData(serverLevel).markAttempting(pos, direction, false, tick);
        CooperativePistonData.markAttemptingClient(pos, direction, false, tick);
    }

    /**
     * Pre-retract each cooperator before the resolver runs. With the cooperator's head still
     * present, the cooperative sticky-branching forward-scan from one column hits the other
     * column's PISTON_HEAD (push reaction BLOCK) and fails. We mimic what each cooperator's
     * own triggerEvent would do moments later: body → MOVING_PISTON, then head → AIR.
     * The body-first order matters — removing the head while the body is still STICKY_PISTON
     * makes {@code PistonHeadBlock.onRemove} destroyBlock() the body and pop it as an item.
     */
    @Inject(method = "moveBlocks", at = @At("HEAD"))
    private void supp$preRetractCooperators(Level level, BlockPos pos, Direction facing, boolean extending,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return;
        if (extending) return;
        Set<BlockPos> cooperators = supp$lookupCooperators(level, pos, facing, false);
        if (cooperators.isEmpty()) return;
        for (BlockPos cooperatorPos : cooperators) {
            supp$preRetractCooperator(level, cooperatorPos, facing);
        }
    }

    /**
     * Set cooperators on the resolver just before {@code resolve()} is called. Using @Inject
     * + @Local here (instead of wrapping the {@code new PistonStructureResolver}) means we
     * see whatever resolver actually ends up assigned to the local — including Quark/Zeta's
     * wrapper if its {@code @ModifyExpressionValue} ran on the NEW.
     */
    @Inject(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"))
    private void supp$applyCoopBeforeResolve(Level level, BlockPos pos, Direction facing, boolean extending,
                                             CallbackInfoReturnable<Boolean> cir,
                                             @Local PistonStructureResolver resolver) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return;
        Set<BlockPos> cooperators = supp$lookupCooperators(level, pos, facing, extending);
        if (cooperators.isEmpty()) return;
        Set<BlockPos> allPistons = new HashSet<>(cooperators);
        allPistons.add(pos);
        int limit = allPistons.size() * 12;
        ((ICooperativePiston) resolver).supp$setCooperators(allPistons, limit, facing, extending);
        Supplementaries.LOGGER.info("[COOP] moveBlocks@{} facing={} extending={} cooperators={}",
                pos, facing, extending, cooperators);
    }

    @Unique
    private static Set<BlockPos> supp$lookupCooperators(Level level, BlockPos pos, Direction facing, boolean extending) {
        if (level instanceof ServerLevel serverLevel) {
            return ModData.COOPERATIVE_PISTONS.getData(serverLevel)
                    .getCooperators(pos, facing, extending, level.getGameTime());
        }
        return CooperativePistonData.getCooperatorsClient(pos, facing, extending);
    }

    @Unique
    private static void supp$preRetractCooperator(Level level, BlockPos cooperatorPos, Direction facing) {
        BlockState bodyState = level.getBlockState(cooperatorPos);
        if (!(bodyState.getBlock() instanceof PistonBaseBlock)) return;

        BlockPos headPos = cooperatorPos.relative(facing);
        if (!level.getBlockState(headPos).is(Blocks.PISTON_HEAD)) return;

        PistonType type = bodyState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
        BlockState movingPistonState = Blocks.MOVING_PISTON.defaultBlockState()
                .setValue(MovingPistonBlock.FACING, facing)
                .setValue(MovingPistonBlock.TYPE, type);
        BlockState retractedBody = bodyState.setValue(PistonBaseBlock.EXTENDED, false);
        level.setBlock(cooperatorPos, movingPistonState, 20);
        level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(
                cooperatorPos, movingPistonState, retractedBody, facing, false, true));

        level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 20);
    }
}
