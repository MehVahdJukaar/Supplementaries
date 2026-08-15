package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICooperativePiston;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PistonCooperationData;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
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

import java.util.HashSet;
import java.util.Set;

@Mixin(PistonBaseBlock.class)
public class CooperativePistonBaseBlockMixin {

    // if a piston can't extend alone, retry the resolve together with the ones that registered
    // earlier this tick. reusing the same resolver keeps any Quark/Zeta wrapping on it
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
        PistonCooperationData data = PistonCooperationData.get(serverLevel);
        data.markAttempting(pos, direction, true, tick);

        if (original.call(resolver)) {
            data.markPosted(pos, tick);
            return true;
        }

        Set<BlockPos> cooperators = PistonCooperationData.getCooperators(level, pos, direction, true, tick);
        if (cooperators.isEmpty()) return false;
        if (!supp$resolveTogether(resolver, cooperators, pos, direction, true)) return false;

        // only the ones that actually shared the pushed structure get fired
        Iterable<BlockPos> contributing = ((ICooperativePiston) resolver).supp$getCoopState().getContributingCooperators();
        for (BlockPos cooperatorPos : contributing) {
            if (data.hasPosted(cooperatorPos, tick)) continue;
            level.blockEvent(cooperatorPos, level.getBlockState(cooperatorPos).getBlock(), 0, direction.get3DDataValue());
            data.markPosted(cooperatorPos, tick);
        }
        data.markPosted(pos, tick);
        return true;
    }

    // retraction never makes a resolver, it just posts a block event, so hook that to register
    @Inject(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V",
                    ordinal = 1))
    private void supp$registerRetraction(Level level, BlockPos pos, BlockState state, CallbackInfo ci,
                                         @Local Direction direction) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        PistonCooperationData.get(serverLevel).markAttempting(pos, direction, false, level.getGameTime());
    }

    // extension was already decided in checkIfExtend so we just re-apply the cooperators here.
    // retraction wasn't, and a lone sticky piston normally pulls fine, so try it alone first and
    // only cooperate if that fails. otherwise parallel retractions force-cooperate and orphan
    // each other's pulled block
    @WrapOperation(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"))
    private boolean supp$gateCoopOnResolve(PistonStructureResolver resolver, Operation<Boolean> original,
                                           @Local(argsOnly = true) Level level,
                                           @Local(argsOnly = true) BlockPos pos,
                                           @Local(argsOnly = true) Direction facing,
                                           @Local(argsOnly = true) boolean extending) {
        if (!CommonConfigs.Tweaks.COOPERATIVE_PISTONS.get()) return original.call(resolver);

        Set<BlockPos> cooperators = PistonCooperationData.getCooperators(level, pos, facing, extending, level.getGameTime());
        if (cooperators.isEmpty()) return original.call(resolver);

        if (!extending) {
            if (original.call(resolver)) return true;
            // the other columns' heads are still there and would block the scan, so take them out
            // early like their own triggerEvent is about to do
            for (BlockPos cooperatorPos : cooperators) {
                supp$preRetractCooperator(level, cooperatorPos, facing);
            }
        }
        return supp$resolveTogether(resolver, cooperators, pos, facing, extending);
    }

    @Unique
    private static boolean supp$resolveTogether(PistonStructureResolver resolver, Set<BlockPos> cooperators,
                                                BlockPos pos, Direction facing, boolean extending) {
        Set<BlockPos> allPistons = new HashSet<>(cooperators);
        allPistons.add(pos);
        ((ICooperativePiston) resolver).supp$setCooperators(allPistons, facing, extending);
        return resolver.resolve();
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

        // after the body, else PistonHeadBlock.onRemove pops the body as an item
        level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 20);
    }
}
