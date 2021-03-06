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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(EatGrassGoal.class)
public abstract class EatGrassGoalMixin extends Goal {

    @Shadow
    private MobEntity grassEaterEntity;

    @Shadow
    private int eatingGrassTimer;

    @Shadow
    private static Predicate<BlockState> IS_GRASS;

    @Shadow
    private World entityWorld;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo info) {
        if (this.eatingGrassTimer == 5) {
            BlockPos blockpos = this.grassEaterEntity.getPosition().down();
            if (IS_GRASS.test(this.entityWorld.getBlockState(blockpos.up())))return;
            if (this.entityWorld.getBlockState(blockpos).getBlock() instanceof FodderBlock) {
                this.entityWorld.playEvent(2001, blockpos, Block.getStateId(Registry.FODDER.get().getDefaultState()));
                this.grassEaterEntity.eatGrassBonus();
                this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);
                info.cancel();
            }
        }
    }
    @Inject(method = "shouldExecute", at = @At("HEAD"), cancellable = true)
    public void shouldExecute(CallbackInfoReturnable<Boolean> info) {
        if (this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? 50 : 950) == 0) {
            BlockPos blockpos = this.grassEaterEntity.getPosition().down();
            boolean flag = this.entityWorld.getBlockState(blockpos).getBlock() instanceof FodderBlock;
            if(flag)info.setReturnValue(true);

        }
    }
}