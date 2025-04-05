package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolemRenderer.class)
public abstract class IronGolemRendererMixin {

    @Inject(method = "getTextureLocation*", at = @At("HEAD"), cancellable = true)
    public void supp$swag(@NotNull IronGolem entity, CallbackInfoReturnable<ResourceLocation> info) {
        if (entity.getUUID().getLeastSignificantBits() % 420 == 0 && !ClientConfigs.General.UNFUNNY.get())
            info.setReturnValue(ModTextures.THICK_GOLEM);
    }
}