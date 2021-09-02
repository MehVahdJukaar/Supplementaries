package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.client.model.data.EmptyModelData;




public class FireflyJarItemRenderer extends ItemStackTileEntityRenderer {


    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.pushPose();
        BlockState state = ModRegistry.FIREFLY_JAR.get().defaultBlockState();
        Minecraft.getInstance().getBlockRenderer().renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        float r = 1;
        float g = 1;
        float b = 1;
        float a = 1;
        matrixStackIn.translate(0.5, 0.5-0.125, 0.5);
        matrixStackIn.scale(0.25f, 0.25f, 0.25f);

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.entityTranslucent(Textures.FIREFLY_TEXTURE));

        if(transformType.equals(ItemCameraTransforms.TransformType.FIXED)) {
            Quaternion rotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(rotation);
            RendererUtil.addQuadSide(ivertexbuilder, matrixStackIn, -0.5f, -0.5f, 0, 0.5f, 0.5f, 0, 0, 0, 1, 1,  r,  g, b, a, 240, 0, 0, 1, 0);
            matrixStackIn.popPose();
            matrixStackIn.mulPose(Const.Y90);
            matrixStackIn.mulPose(rotation);
            //TODO: fix this like compass item does

        }
        else{
            matrixStackIn.mulPose(Const.YN45);
        }


        RendererUtil.addQuadSide(ivertexbuilder, matrixStackIn, -0.5f, -0.5f, 0, 0.5f, 0.5f, 0, 0, 0, 1, 1,  r,  g, b, a, 240, 0, 0, 1, 0);

        matrixStackIn.popPose();
    }
}