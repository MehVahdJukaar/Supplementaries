package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(IronGolemRenderer.class)
public abstract class IronGolemRendererMixin {

    @Inject(method = "getTextureLocation*", at = @At("HEAD"), cancellable = true)
    public void getEntityTexture(@Nonnull IronGolem entity, CallbackInfoReturnable<ResourceLocation> info) {
        if (entity.getUUID().getLeastSignificantBits() % 420 == 0)
            info.setReturnValue(Textures.THICK_GOLEM);
    }
}