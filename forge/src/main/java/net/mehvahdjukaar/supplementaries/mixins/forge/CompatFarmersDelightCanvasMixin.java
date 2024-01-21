package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HangingSignRendererExtension;
import net.mehvahdjukaar.supplementaries.common.block.IExtendedHangingSign;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vectorwing.farmersdelight.client.renderer.HangingCanvasSignRenderer;

import javax.annotation.Nullable;
import java.util.List;


@Pseudo
@Mixin(HangingCanvasSignRenderer.class)
public abstract class CompatFarmersDelightCanvasMixin extends SignRenderer {

    @Unique
    private List<ModelPart> supplementaries$barModel;
    @Unique
    private ModelPart supplementaries$chains;

    protected CompatFarmersDelightCanvasMixin(BlockEntityRendererProvider.Context arg) {
        super(arg);
    }

    @Shadow
    public abstract Material getCanvasSignMaterial(@Nullable DyeColor dyeColor);


    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/client/renderer/HangingCanvasSignRenderer;renderSignWithText(Lnet/minecraft/world/level/block/entity/SignBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/SignBlock;Lnet/minecraft/world/item/DyeColor;Lnet/minecraft/client/model/Model;)V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    protected void renderSignWithText(SignBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                                      int packedLight, int packedOverlay, CallbackInfo ci,
                                      BlockState state, SignBlock sign, HangingSignRenderer.HangingSignModel model,
                                      DyeColor dye) {

        if (ClientConfigs.Blocks.ENHANCED_HANGING_SIGNS.get() && ((IExtendedHangingSign) tile).getExtension().canSwing()) {
            BlockState blockState = tile.getBlockState();

            HangingSignRendererExtension.render(tile, partialTick,
                    poseStack, bufferSource, packedLight, packedOverlay,
                    blockState, model, supplementaries$barModel, supplementaries$chains,

                    this.getCanvasSignMaterial(dye),
                    ModMaterials.CANVAS_SIGH_MATERIAL,
                    this, 0.6f/0.4f*ClientConfigs.getSignColorMult());
            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initEnhancedSign(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        if (PlatHelper.isModLoadingValid()) {
            ModelPart model = context.bakeLayer(ClientRegistry.HANGING_SIGN_EXTENSION);
            this.supplementaries$barModel = List.of(model.getChild("extension_6"),
                    model.getChild("extension_5"),
                    model.getChild("extension_4"),
                    model.getChild("extension_3"));
            this.supplementaries$chains = context.bakeLayer(ClientRegistry.HANGING_SIGN_EXTENSION_CHAINS);
        }
    }
}
