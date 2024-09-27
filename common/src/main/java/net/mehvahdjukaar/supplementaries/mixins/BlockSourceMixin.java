package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.misc.IMovingBlockSource;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockSource.class)
public abstract class BlockSourceMixin implements IMovingBlockSource {

    @Nullable
    @Unique
    private Entity supp$entity;

    @Override
    public void supp$setEntity(@Nullable Entity entity) {
        this.supp$entity = entity;
    }

    @Nullable
    @Override
    public Entity supp$getEntity() {
        return this.supp$entity;
    }

    @Inject(method = "center", at = @At("HEAD"), cancellable = true)
    private void supp$alterCenter(CallbackInfoReturnable<Vec3> cir) {
        if (this.supp$entity != null) {
            cir.setReturnValue(this.supp$entity.position());
        }
    }
}
