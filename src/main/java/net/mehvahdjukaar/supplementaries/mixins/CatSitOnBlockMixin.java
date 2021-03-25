package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.PlanterBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitOnBlockMixin extends MoveToBlockGoal {

    public CatSitOnBlockMixin(CreatureEntity creature, double speedIn, int length) {
        super(creature, speedIn, length);
    }

    private boolean doormat=false;

    @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
    protected void shouldMoveTo(IWorldReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
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
