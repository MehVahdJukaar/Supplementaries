package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.BellowsBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.BlackboardBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HangingSignRendererExtension;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(HangingSignRenderer.class)
public abstract class HangingSignRendererMixin extends SignRenderer {

    @Shadow @Final private Map<WoodType, HangingSignRenderer.HangingSignModel> hangingSignModels;

    @Shadow abstract Material getSignMaterial(WoodType woodType);

    @Unique
    private List<ModelPart> barModel;
    @Unique
    private ModelPart chains;

    protected HangingSignRendererMixin(BlockEntityRendererProvider.Context context) {
        super(context);
    }


    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"), cancellable = true)
    public void renderEnhancedSign( SignBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, int packedOverlay, CallbackInfo ci) {
        if(ClientConfigs.Tweaks.EXTENDED_HANGING_SIGN.get()) {
            BlockState blockState = blockEntity.getBlockState();
            WoodType woodType = SignBlock.getWoodType(blockState.getBlock());
            HangingSignRenderer.HangingSignModel model = this.hangingSignModels.get(woodType);

            HangingSignRendererExtension.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay,
                    blockState, model, barModel,chains,

                    this.getSignMaterial(woodType),
                    ModMaterials.HANGING_SIGN_EXTENSIONS.get().get(woodType),
                    this);

            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initEnhancedSign(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        ModelPart model = context.bakeLayer(ClientRegistry.HANGING_SIGN_EXTENSION);
        this.barModel = List.of(model.getChild("extension_6"),
                model.getChild("extension_5"),
                model.getChild("extension_4"),
                model.getChild("extension_3"));
        this.chains = context.bakeLayer(ClientRegistry.HANGING_SIGN_EXTENSION_CHAINS);
    }

}
