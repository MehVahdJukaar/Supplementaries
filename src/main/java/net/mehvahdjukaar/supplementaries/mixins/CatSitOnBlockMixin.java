package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.blocks.PlanterBlock;
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

    @Inject(method = "shouldMoveTo", at = @At("HEAD"), cancellable = true)
    protected void shouldMoveTo(IWorldReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        Block block = worldIn.getBlockState(pos).getBlock();
        this.doormat=block instanceof DoormatBlock;
        if (block instanceof PlanterBlock || this.doormat) {
            info.setReturnValue(true);
        }
    }

    @Override
    public double getTargetDistanceSq() {
        return this.doormat?0.8:this.getTargetDistanceSq();
    }

    //bug fix
    @Override
    protected BlockPos func_241846_j() {
        return this.destinationBlock;
    }

    /*
    @Override
    public double getTargetDistanceSq() {
        return this.doormat?1.5:this.getTargetDistanceSq();
    }*/

    /*
    public void tick() {
        try {
            Field f = ObfuscationReflectionHelper.findField(MoveToBlockGoal.class,"field_179491_g");
            CommonUtil.deb(1);
            f.setAccessible(true);

            BlockPos blockpos = this.func_241846_j();//
            if (!CommonUtil.withinDistanceDown(blockpos,this.creature.getPositionVec(),this.getTargetDistanceSq(),2)) {
                CommonUtil.deb(2);
                f.setBoolean(this, false);
                ++this.timeoutCounter;
                if (this.shouldMove()) {
                    this.creature.getNavigator().tryMoveToXYZ((double)((float)blockpos.getX()) + 0.5D, (double)blockpos.getY(), (double)((float)blockpos.getZ()) + 0.5D, this.movementSpeed);
                }
            } else {
                CommonUtil.deb(3);
                f.setBoolean(this, true);
                --this.timeoutCounter;
            }

        }
        catch (Exception ignored){
            super.tick();
        };


    }*/
}
