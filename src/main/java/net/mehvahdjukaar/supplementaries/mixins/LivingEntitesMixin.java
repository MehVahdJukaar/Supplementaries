package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitesMixin extends Entity {

    public LivingEntitesMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Shadow
    public abstract BlockState getFeetBlockState();

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract boolean isSuppressingSlidingDownLadder();

    @Inject(method = "handleOnClimbable", at = @At("HEAD"), cancellable = true)
    private void handleOnClimbable(Vector3d motion, CallbackInfoReturnable<Vector3d> info) {
        if (this.onClimbable() && this.getFeetBlockState().getBlock() instanceof RopeBlock) {
            this.fallDistance = 0;
            double x = MathHelper.clamp(motion.x, -0.15F, 0.15F);
            double z = MathHelper.clamp(motion.z, -0.15F, 0.15F);
            double y = motion.y();
            if (y < 0 && this.isSuppressingSlidingDownLadder() && this.getEntity() instanceof PlayerEntity) y = 0;
            info.setReturnValue(new Vector3d(x, y, z));
        }
    }


}
