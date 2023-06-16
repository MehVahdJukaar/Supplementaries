package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PageButton.class)
public abstract class PageButtonMixin implements IAntiqueTextProvider {

    @Unique
    private boolean antiqueInk;

    @Override
    public boolean hasAntiqueInk() {
        return antiqueInk;
    }

    @Override
    public void setAntiqueInk(boolean hasInk) {
        this.antiqueInk = hasInk;
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    ResourceLocation setTatteredBookTexture(ResourceLocation res) {
        if(antiqueInk){
           return ModTextures.TATTERED_BOOK_GUI_TEXTURE;
        }
        return res;
    }
}
