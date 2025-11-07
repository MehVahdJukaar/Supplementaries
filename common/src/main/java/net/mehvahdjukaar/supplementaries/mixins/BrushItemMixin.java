package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.block.ISimpleBrushable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrushItem.class)
public class BrushItemMixin {

    @WrapOperation(method = "onUseTick",

            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BrushItem;spawnDustParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/HumanoidArm;)V"))
    public void supp$blackboardBrush(BrushItem instance, Level level, BlockHitResult hit, BlockState state,
                                     Vec3 viewVec, HumanoidArm arm, Operation<Void> original,
                                     @Local BlockPos pos, @Local(argsOnly = true) ItemStack stack,
                                     @Local Player livingEntity) {
        if (state.getBlock() instanceof ISimpleBrushable sb) {
            BrushItem.DustParticlesDelta d = BrushItem.DustParticlesDelta.fromDirection(viewVec, hit.getDirection());
            if (sb.brush(state, pos, level, stack,  livingEntity, arm, hit, new Vec3(d.xd(), d.yd(), d.zd()))) {
                return;
            }
        }
        original.call(instance, level, hit, state, viewVec, arm);
    }
}
