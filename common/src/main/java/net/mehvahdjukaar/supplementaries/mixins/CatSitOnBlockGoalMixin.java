package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitOnBlockGoalMixin extends MoveToBlockGoal {

    protected CatSitOnBlockGoalMixin(PathfinderMob creature, double speedIn, int length) {
        super(creature, speedIn, length);
    }

    @Unique
    private boolean supplementaries$doormat = false;

    // TODO: test
    @Inject(method = "isValidTarget",
            at = @At(value = "INVOKE",
            shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
            ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true)
    protected void shouldMoveTo(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState) {
        Block block = blockState.getBlock();
        this.supplementaries$doormat = block instanceof DoormatBlock;
        if (this.supplementaries$doormat || block instanceof PlanterBlock) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public double acceptedDistance() {
        return this.supplementaries$doormat ? 0.8 : super.acceptedDistance();
    }


}
