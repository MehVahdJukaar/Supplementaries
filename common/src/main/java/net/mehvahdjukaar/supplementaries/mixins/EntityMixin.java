package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract void playSound(SoundEvent pSound, float pVolume, float pPitch);

    @Shadow
    public abstract Level getLevel();

    @Unique
    private BlockState cachedBlockState;

    //cancel rope slide down sound
    @Inject(method = "playStepSound", at = @At(value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void playStepSound(BlockPos pPos, BlockState state, CallbackInfo ci) {
        //adds it for ash like snow
        if (cachedBlockState.is(ModRegistry.ASH_BLOCK.get())) {
            SoundType soundtype = cachedBlockState.getSoundType();
            this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
            ci.cancel();
            return;
        }
        //cancels for rope
        if (state.is(ModRegistry.ROPE.get()) && (Entity) (Object) this instanceof LivingEntity le
                && le.onClimbable() && le.yya <= 0) {
            ci.cancel();
        }
    }

    //cancel rope slide down sound
    @ModifyVariable(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), argsOnly = true)
    private BlockState getBlockstate(BlockState state) {
        this.cachedBlockState = state;
        return state;
    }
}
