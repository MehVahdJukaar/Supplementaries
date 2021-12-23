package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRendererDispatcher {


    @Final
    @Shadow
    public Font font;


    private static boolean antiqueFontActive;
    //private static final IAntiqueTextProvider FONT = (IAntiqueTextProvider) (Minecraft.getInstance().font);


    @Inject(method = "setupAndRender", at = @At("HEAD"))
    private static <T extends BlockEntity> void setupAndRenderPre(BlockEntityRenderer<T> renderer, T tile, float partialTicks,
                                                                  PoseStack matrixStack, MultiBufferSource buffer, CallbackInfo ci) {
        CapabilityHandler.doStuff(tile, () -> antiqueFontActive = true);

    }

    @Inject(method = "setupAndRender", at = @At("RETURN"))
    private static <T extends BlockEntity> void setupAndRenderPost(BlockEntityRenderer<T> renderer, T tile, float partialTicks,
                                                                   PoseStack matrixStack, MultiBufferSource buffer, CallbackInfo ci) {
        if (antiqueFontActive) {
            IAntiqueTextProvider FONT = (IAntiqueTextProvider) (Minecraft.getInstance().font);
            FONT.setAntiqueInk(false);
            antiqueFontActive = false;
        }
    }
}