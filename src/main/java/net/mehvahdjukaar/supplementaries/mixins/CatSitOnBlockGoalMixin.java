package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitOnBlockGoalMixin extends MoveToBlockGoal {

    public CatSitOnBlockGoalMixin(PathfinderMob creature, double speedIn, int length) {
        super(creature, speedIn, length);
    }

    private boolean doormat=false;

    @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
    protected void shouldMoveTo(LevelReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        Block block = worldIn.getBlockState(pos).getBlock();
        this.doormat=block instanceof DoormatBlock;
        if (block instanceof PlanterBlock || this.doormat) {
            info.setReturnValue(true);
        }
    }

    @Override
    public double acceptedDistance() {
        return this.doormat?0.8:super.acceptedDistance();
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return this.doormat?this.blockPos:this.blockPos;
    }

}
