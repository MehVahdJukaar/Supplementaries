package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.minecraftforge.common.extensions.IForgeFluid;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRendererDispatcherMixin {
    
    @Unique
    private static boolean antiqueFontActive;

    @Inject(method = "setupAndRender", at = @At("HEAD"))
    private static <T extends BlockEntity> void setupAndRenderPre(BlockEntityRenderer<T> renderer, T tile, float partialTicks,
                                                                  PoseStack matrixStack, MultiBufferSource buffer, CallbackInfo ci) {
        tile.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP).ifPresent(c -> {
            if (c.hasAntiqueInk()) {
                IAntiqueTextProvider font = (IAntiqueTextProvider) (Minecraft.getInstance().font);
                font.setAntiqueInk(true);
                antiqueFontActive = true;
            }
        });
    }

    @Inject(method = "setupAndRender", at = @At("RETURN"))
    private static <T extends BlockEntity> void setupAndRenderPost(BlockEntityRenderer<T> renderer, T tile, float partialTicks,
                                                                   PoseStack matrixStack, MultiBufferSource buffer, CallbackInfo ci) {
        if (antiqueFontActive) {
            IAntiqueTextProvider font = (IAntiqueTextProvider) (Minecraft.getInstance().font);
            font.setAntiqueInk(false);
            antiqueFontActive = false;
        }
    }
}
