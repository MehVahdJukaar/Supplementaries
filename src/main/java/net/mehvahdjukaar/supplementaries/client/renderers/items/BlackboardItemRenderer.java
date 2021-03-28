package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.client.model.data.EmptyModelData;


public class BlackboardItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0,0,-0.34375);
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState state = Registry.BLACKBOARD.get().defaultBlockState();
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

        CompoundNBT com = stack.getTagElement("BlockEntityTag");
        if(com != null && com.contains("pixels_0")) {

            IVertexBuilder builder = bufferIn.getBuffer(RenderType.entitySolid(Textures.BLACKBOARD_TEXTURE));

            int lu = combinedLightIn & '\uffff';
            int lv = combinedLightIn >> 16 & '\uffff'; // ok

            matrixStackIn.translate(1, 1, 0.6875);
            matrixStackIn.scale(-1, -1, 1);

            float w = 1 / 16f;
            for (int x = 0; x < 16; x++) {
                byte[] pixels = com.getByteArray("pixels_"+x);
                for (int y = 0; y < 16; y++) {

                    float x0 = x * w;
                    float x1 = (x + 1) * w;
                    float y0 = y * w;
                    float y1 = (y + 1) * w;
                    float offset = pixels[y] > 0 ? 0.5f : 0;

                    int rgb = BlackboardBlock.colorFromByte(pixels[y]);
                    float b = NativeImage.getR(rgb)/255f;
                    float g = NativeImage.getG(rgb)/255f;
                    float r = NativeImage.getB(rgb)/255f;

                    RendererUtil.addQuadSide(builder, matrixStackIn, x1, y0, 0, x0, y1, 0, offset + x0 / 2f, y0, offset + x1 / 2f, y1, r, g, b, 1, lu, lv, 0, 0, 1);

                }
            }
        }
        matrixStackIn.popPose();

    }
}