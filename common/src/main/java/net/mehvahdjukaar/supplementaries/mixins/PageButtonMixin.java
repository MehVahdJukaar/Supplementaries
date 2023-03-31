package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.screens.inventory.PageButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "renderButton", at = @At(value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    void setTatteredBookTexture(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if(antiqueInk){
            RenderSystem.setShaderTexture(0, ModTextures.TATTERED_BOOK_GUI_TEXTURE);
        }
    }
}
