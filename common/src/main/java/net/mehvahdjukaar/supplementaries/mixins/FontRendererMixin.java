package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

//@Mixin(targets = {"net/minecraft/client/gui/FontRenderer$CharacterRenderer"})
@Mixin(Font.class)
public abstract class FontRendererMixin implements IAntiqueTextProvider {

    @Unique
    private boolean antique = false;

    @Override
    public boolean hasAntiqueInk() {
        return antique;
    }

    @Override
    public void setAntiqueInk(boolean hasInk) {
        antique = hasInk;
    }

    @Final
    @Shadow
    private Function<ResourceLocation, FontSet> fonts;

    @Inject(method = "getFontSet", at = @At("HEAD"), cancellable = true)
    private void getFontSet(ResourceLocation resourceLocation, CallbackInfoReturnable<FontSet> cir) {
        if (antique) {
            cir.setReturnValue(this.fonts.apply(Textures.ANTIQUABLE_FONT));
        }
    }

}