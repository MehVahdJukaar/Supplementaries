package net.mehvahdjukaar.supplementaries.renderers;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;


@OnlyIn(Dist.CLIENT)
public class MobJarItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = Registry.FIREFLY_JAR.getDefaultState();
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        float r = 1;
        float g = 1;
        float b = 1;
        float a = 1;
        EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.translate(0, -0.1, 0);
        // matrixStackIn.rotate(renderManager.getCameraOrientation());
        // renderManager.renderEntityStatic(new
        // FireflyEntity.CustomEntity(FireflyEntity.entity,
        // (World)Minecraft.getInstance().world), 0d, 0d, 0d, 0f, 1f, matrixStackIn,
        // bufferIn, combinedLightIn);
        // TextureAtlasSprite sprite =
        // Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_PARTICLES_TEXTURE).apply(texture);
        matrixStackIn.scale(0.6f, 0.6f, 0.6f);
        // matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
        float f9 = 0.32F;
        // matrixStackIn.scale(0.3F, 0.3F, 0.3F);
        float minu = 0;// sprite.getMinU();
        float minv = 0;// sprite.getMinV();
        float maxu = 1;// sprite.getMaxU();
        float maxv = 1;// sprite.getMaxV();

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityTranslucent(CommonUtil.FIREFLY_TEXTURE));

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-45));

        RendererUtil.addQuadSide(ivertexbuilder, matrixStackIn, -0.5f, -0.5f, 0, 0.5f, 0.5f, 0, 0, 0, 1, 1,  r,  g, b, a, 240, 0, 0, 1, 0);

        matrixStackIn.pop();
    }
}