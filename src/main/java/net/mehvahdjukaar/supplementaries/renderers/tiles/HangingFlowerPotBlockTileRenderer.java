package net.mehvahdjukaar.supplementaries.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.HangingFlowerPotBlock;
import net.mehvahdjukaar.supplementaries.blocks.OilLanternBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.tiles.OilLanternBlockTile;
import net.mehvahdjukaar.supplementaries.renderers.Const;
import net.mehvahdjukaar.supplementaries.renderers.RendererUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class HangingFlowerPotBlockTileRenderer extends TileEntityRenderer<HangingFlowerPotBlockTile> {
    public HangingFlowerPotBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(HangingFlowerPotBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = tile.pot;
        BlockState state2 = tile.getBlockState().with(HangingFlowerPotBlock.TILE,true);

        matrixStackIn.push();
        matrixStackIn.translate(0.5, 0.5, 0.5);

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle*1.5f, tile.angle*1.5f)));
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        // render block
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        //blockRenderer.renderBlock(state2, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        RendererUtil.renderBlockPlus(state, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos());
        RendererUtil.renderBlockPlus(state2, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos(), RenderType.getCutout());

        matrixStackIn.pop();




    }
}