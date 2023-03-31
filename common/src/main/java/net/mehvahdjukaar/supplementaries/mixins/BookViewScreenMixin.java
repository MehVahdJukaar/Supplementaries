package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {

    @Shadow
    private BookViewScreen.BookAccess bookAccess;

    @Shadow
    protected abstract void init();

    @Shadow private PageButton forwardButton;

    @Shadow private PageButton backButton;

    @Inject(method = "render", at = @At(value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    public void setTatteredBookTexture(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.bookAccess instanceof IAntiqueTextProvider wb && wb.hasAntiqueInk()) {
            RenderSystem.setShaderTexture(0, ModTextures.TATTERED_BOOK_GUI_TEXTURE);
            ((IAntiqueTextProvider) this.forwardButton).setAntiqueInk(true);
            ((IAntiqueTextProvider) this.backButton).setAntiqueInk(true);
        }
    }
}
