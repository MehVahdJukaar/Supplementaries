package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract void playSound(SoundEvent pSound, float pVolume, float pPitch);

    //cancel rope slide down sound
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void playStepSound(BlockPos pPos, BlockState state, CallbackInfo ci) {
        //cancels for rope
        if (state.is(ModRegistry.ROPE.get()) && (Entity) (Object) this instanceof LivingEntity le
                && le.onClimbable() && le.yya <= 0) {
            ci.cancel();
        } //TODO: check ash
    }

}
