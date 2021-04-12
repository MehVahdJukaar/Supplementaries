package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.FodderBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(EatGrassGoal.class)
public abstract class EatGrassGoalMixin extends Goal {

    @Final
    @Shadow
    private MobEntity mob;

    @Shadow
    private int eatAnimationTick;

    @Final
    @Shadow
    private static Predicate<BlockState> IS_TALL_GRASS;

    @Final
    @Shadow
    private World level;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo info) {
        if (this.eatAnimationTick == 5) {
            BlockPos blockpos = this.mob.blockPosition().below();
            if (IS_TALL_GRASS.test(this.level.getBlockState(blockpos.above())))return;
            if (this.level.getBlockState(blockpos).getBlock() instanceof FodderBlock) {
                this.level.levelEvent(2001, blockpos, Block.getId(Registry.FODDER.get().defaultBlockState()));
                this.mob.ate();
                this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
                info.cancel();
            }
        }
    }
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void shouldExecute(CallbackInfoReturnable<Boolean> info) {
        if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 950) == 0) {
            BlockPos blockpos = this.mob.blockPosition().below();
            boolean flag = this.level.getBlockState(blockpos).getBlock() instanceof FodderBlock;
            if(flag)info.setReturnValue(true);

        }
    }
}