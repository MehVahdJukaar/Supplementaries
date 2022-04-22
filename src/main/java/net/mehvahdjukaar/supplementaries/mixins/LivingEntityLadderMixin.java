package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityLadderMixin extends Entity {

    public LivingEntityLadderMixin(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract boolean isSuppressingSlidingDownLadder();

    @Inject(method = "handleOnClimbable", at = @At("HEAD"), cancellable = true)
    private void handleOnClimbable(Vec3 motion, CallbackInfoReturnable<Vec3> info) {
        if (this.onClimbable() && ServerConfigs.cached.ROPE_SLIDE) {
            BlockState b = this.getFeetBlockState();
            if (b.is(ModRegistry.ROPE.get())) {
                this.fallDistance = 0;
                double x = Mth.clamp(motion.x, -0.15F, 0.15F);
                double z = Mth.clamp(motion.z, -0.15F, 0.15F);
                double y = motion.y();
                if (y < 0 && this.isSuppressingSlidingDownLadder() && this.getEntity() instanceof Player) y = 0;
                info.setReturnValue(new Vec3(x, y, z));
            }
        }
    }

    private Entity getEntity() {
        return this;
    }
}
