package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PageButton.class)
public abstract class PageButtonMixin extends Button implements IAntiquable {

    @Shadow
    @Final
    private boolean isForward;
    @Unique
    private boolean supplementaries$antiqueInk;

    protected PageButtonMixin(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    @Override
    public boolean supplementaries$isAntique() {
        return supplementaries$antiqueInk;
    }

    @Override
    public void supplementaries$setAntique(boolean hasInk) {
        this.supplementaries$antiqueInk = hasInk;
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    ResourceLocation supp$setTatteredBookTexture(ResourceLocation res) {
        if (supplementaries$antiqueInk) {
            if (this.isForward) {
                return this.isHoveredOrFocused() ? ModTextures.TATTERED_PAGE_FORWARD_HIGHLIGHTED_SPRITE : ModTextures.TATTERED_PAGE_FORWARD_SPRITE;
            } else {
                return this.isHoveredOrFocused() ? ModTextures.TATTERED_PAGE_BACKWARD_HIGHLIGHTED_SPRITE : ModTextures.TATTERED_PAGE_BACKWARD_SPRITE;
            }
        }
        return res;
    }
}
