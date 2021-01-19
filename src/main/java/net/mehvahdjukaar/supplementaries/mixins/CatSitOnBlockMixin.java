package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitOnBlockMixin extends MoveToBlockGoal {


    public CatSitOnBlockMixin(CreatureEntity creature, double speedIn, int length) {
        super(creature, speedIn, length);
    }

    @Inject(method = "shouldMoveTo", at = @At("HEAD"), cancellable = true)
    protected void shouldMoveTo(IWorldReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        Block block = worldIn.getBlockState(pos).getBlock();
        if (block instanceof PlanterBlock || block instanceof DoormatBlock) {
            info.setReturnValue(true);
        }
    }

    //for doormat
    @Override
    public double getTargetDistanceSq() {
        return 1.5D;
    }
}
