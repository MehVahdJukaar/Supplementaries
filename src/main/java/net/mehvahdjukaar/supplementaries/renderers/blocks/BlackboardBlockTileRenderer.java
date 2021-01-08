package net.mehvahdjukaar.supplementaries.renderers.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.tiles.ItemShelfBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.renderers.Const;
import net.mehvahdjukaar.supplementaries.renderers.RendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlackboardBlockTileRenderer extends TileEntityRenderer<BlackboardBlockTile> {
    public BlackboardBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(BlackboardBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntitySolid(Resources.BLACKBOARD_TEXTURE));

        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff'; // ok

        matrixStackIn.push();
        matrixStackIn.translate(0.5,0.5,0.5);

        matrixStackIn.rotate(tile.getBlockState().get(BlackboardBlock.FACING).getOpposite().getRotation());
        matrixStackIn.rotate(Const.XN90);
        matrixStackIn.translate(0.5,0.5,0.25);
        matrixStackIn.scale(-1,-1,1);

        float w = 1/16f;
        for (int x=0; x < tile.pixels.length; x++) {
            for (int y = 0; y < tile.pixels[x].length; y++) {

                float x0 = x * w;
                float x1 = (x + 1) * w;
                float y0 = y * w;
                float y1 = (y + 1) * w;
                float b = tile.pixels[x][y] > 0?0.5f:0;

                RendererUtil.addQuadSide(builder, matrixStackIn, x1, y0, 0, x0, y1, 0, b + x0/2f, y0, b + x1/2f, y1, 1, 1, 1, 1, lu, lv, 0, 0, 1);

            }
        }
        matrixStackIn.pop();

    }
}