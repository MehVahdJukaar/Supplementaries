package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {

    @Shadow
    private BookViewScreen.BookAccess bookAccess;

    @Shadow
    protected abstract void init();

    @Shadow private PageButton forwardButton;

    @Shadow private PageButton backButton;

    @ModifyArg(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    public ResourceLocation setTatteredBookTexture(ResourceLocation resourceLocation) {
        if (this.bookAccess instanceof IAntiqueTextProvider wb && wb.hasAntiqueInk()) {
            ((IAntiqueTextProvider) this.forwardButton).setAntiqueInk(true);
            ((IAntiqueTextProvider) this.backButton).setAntiqueInk(true);
            return ModTextures.TATTERED_BOOK_GUI_TEXTURE;
        }
        return resourceLocation;
    }
}
