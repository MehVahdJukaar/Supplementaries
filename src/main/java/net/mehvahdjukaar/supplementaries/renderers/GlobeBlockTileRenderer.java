package net.mehvahdjukaar.supplementaries.renderers;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlobeBlockTileRenderer extends TileEntityRenderer<GlobeBlockTile> {
    public static final RenderMaterial GLOBE_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, CommonUtil.GLOBE_TEXTURE);

    public final ModelRenderer globe = new ModelRenderer(32, 16, 0, 0);

    public GlobeBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        this.globe.addBox(-4.0F, -29.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        this.globe.setRotationPoint(0.0F, 24.0F, 0.0F);
    }

    @Override
    public void render(GlobeBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        IVertexBuilder builder = GLOBE_TEXTURE.getBuffer(bufferIn, RenderType::getEntityCutoutNoCull);
        matrixStackIn.push();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.rotate(tile.getDirection().getOpposite().getRotation());
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(112.5f));
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180+MathHelper.lerp(partialTicks, tile.prevYaw+tile.face, tile.yaw+tile.face)));
        this.globe.render(matrixStackIn, builder, combinedLightIn,combinedOverlayIn,1,1,1,1);

        //matrixStackIn.translate(-0.5, -0.5, -0.5);
        //BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        //BlockState state = Registry.GLOBE.getDefaultState().with(GlobeBlock.TILE, true);
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();
    }
}