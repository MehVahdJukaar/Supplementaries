package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class HourGlassBlockTileRenderer extends TileEntityRenderer<HourGlassBlockTile> {
    public HourGlassBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(HourGlassBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.push();
        float h = MathHelper.lerp(partialTicks, tile.prevProgress, tile.progress);
        matrixStackIn.translate(0.5,0.125,0.5);
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(CommonUtil.SAND_TEXTURE);

        int color = 0xff00ff;
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucentMovingBlock());
        if(h!=0) {
            RendererUtil.addCube(builder, matrixStackIn, 0.25f, h * 0.25f, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    false, true, true);
        }
        if(h!=1) {
            matrixStackIn.translate(0, 0.5, 0);

            RendererUtil.addCube(builder, matrixStackIn, 0.25f, (1 - h) * 0.25f, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    false, true, true);
        }
        matrixStackIn.pop();
    }
}