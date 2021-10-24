//package net.mehvahdjukaar.supplementaries.client.renderers.entities;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import net.mehvahdjukaar.supplementaries.client.renderers.Const;
//import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
//import net.mehvahdjukaar.supplementaries.common.CommonUtil;
//import net.mehvahdjukaar.supplementaries.common.Textures;
//import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.EntityRenderer;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.Mth;
//
//public class FireflyEntityRenderer extends EntityRenderer<FireflyEntity> {
//
//    protected FireflyEntityRenderer(EntityRendererProvider.Context context) {
//        super(context);
//    }
//
//    @Override
//    public void render(FireflyEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn,
//                       int packedLightIn) {
//
//        matrixStackIn.pushPose();
//
//        float r = CommonUtil.FESTIVITY.isHalloween() ? 0.3f : 1;
//        float g = CommonUtil.FESTIVITY.isHalloween() ? 0 : 1;
//        float b = 1;
//        float a = Mth.lerp(partialTicks, entityIn.prevAlpha, entityIn.alpha);
//
//        matrixStackIn.translate(0.0D, 0.5, 0.0D);
//        matrixStackIn.mulPose(this.entityRenderDispatcher.cameraOrientation());
//        matrixStackIn.mulPose(Const.Y180);
//        float scale = 0.15f;
//        matrixStackIn.scale(a * scale, a * scale, a * scale);
//        VertexConsumer builder = bufferIn.getBuffer(RenderType.beaconBeam(Textures.FIREFLY_TEXTURE, true));
//
//        RendererUtil.addQuadSide(builder, matrixStackIn, -0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f, 0, 0, 1, 1, r, g, b, a, 240, 0, 0, 1, 0);
//
//        matrixStackIn.popPose();
//        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
//    }
//
//
//    @Override
//    public ResourceLocation getTextureLocation(FireflyEntity entity) {
//        return Textures.FIREFLY_TEXTURE;
//    }
//}
