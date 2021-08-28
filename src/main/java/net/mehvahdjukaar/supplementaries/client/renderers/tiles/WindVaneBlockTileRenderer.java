package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class WindVaneBlockTileRenderer extends TileEntityRenderer<WindVaneBlockTile> {

    public static final ResourceLocation MODEL_RES = Supplementaries.res(Registry.WIND_VANE_NAME+"_tile");

    private final BlockRendererDispatcher blockRenderer;
    private final BlockState STATE;

    public WindVaneBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
        STATE = Registry.WIND_VANE.get().defaultBlockState().setValue(WindVaneBlock.TILE, true);
    }

    @Override
    public void render(WindVaneBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90 + MathHelper.lerp(partialTicks, tile.prevYaw, tile.yaw)));
        matrixStackIn.translate(-0.5, -0.5, -0.5);


        RendererUtil.renderBlockModel(STATE, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos(), RenderType.cutout());
        //matrixStackIn.translate(0,0,1);
        //blockRenderer.renderBlock(STATE, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        //matrixStackIn.translate(1,0,-0.5);
        //RendererUtil.renderBlockModel(LabelEntityRenderer.LABEL_LOCATION, matrixStackIn, bufferIn, blockRenderer, combinedLightIn, combinedOverlayIn, false);
        matrixStackIn.popPose();

    }
}