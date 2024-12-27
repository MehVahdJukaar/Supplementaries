package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.neoforge.CapabilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRendererDispatcherMixin {


    @Shadow
    public Level level;
    @Unique
    private static boolean supplementaries$antiqueFontActive;

    @Inject(method = "setupAndRender", at = @At("HEAD"))
    private static <T extends BlockEntity> void setupAndRenderPre(BlockEntityRenderer<T> renderer, T tile, float partialTicks,
                                                                  PoseStack matrixStack, MultiBufferSource buffer, CallbackInfo ci) {
        IAntiquable cap = tile.getLevel().getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP, tile.getBlockPos(),
                tile.getBlockState(), tile);
        if (cap != null) {
            if (cap.supplementaries$isAntique()) {
                IAntiquable font = (IAntiquable) (Minecraft.getInstance().font);
                font.supplementaries$setAntique(true);
                supplementaries$antiqueFontActive = true;
            }
        }
    }

    @Inject(method = "setupAndRender", at = @At("RETURN"))
    private static <T extends BlockEntity> void setupAndRenderPost(BlockEntityRenderer<T> renderer, T tile, float partialTicks,
                                                                   PoseStack matrixStack, MultiBufferSource buffer, CallbackInfo ci) {
        if (supplementaries$antiqueFontActive) {
            IAntiquable font = (IAntiquable) (Minecraft.getInstance().font);
            font.supplementaries$setAntique(false);
            supplementaries$antiqueFontActive = false;
        }
    }
}
