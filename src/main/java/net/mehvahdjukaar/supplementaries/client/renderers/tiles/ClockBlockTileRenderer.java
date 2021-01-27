package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.ClockBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class ClockBlockTileRenderer extends TileEntityRenderer<ClockBlockTile> {
    public static final RenderMaterial HAND_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, Textures.CLOCK_HAND_TEXTURE);
    public final ModelRenderer hourHand = new ModelRenderer(16, 16, 0, 0);
    public final ModelRenderer minuteHand = new ModelRenderer(16, 16, 2, 0);


    public ClockBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

        this.hourHand.addBox(-0.5F, 0.0F, 0.0F, 1.0F, 5.0F, 0.0F, 0.0F, false);
        this.hourHand.setRotationPoint(0.0F, 24.0F, 0.0F);
        this.minuteHand.addBox(-0.5F, 0.0F, 0.0F, 1.0F, 6.0F, 0.0F, 0.0F, false);
        this.minuteHand.setRotationPoint(0.0F, 24.0F, 0.0F);

    }

    @Override

    public void render(ClockBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        IVertexBuilder builder = HAND_TEXTURE.getBuffer(bufferIn, RenderType::getEntityCutoutNoCull);

        matrixStackIn.push();
        matrixStackIn.translate(0.5d, 0.5d, 0.5d);
        matrixStackIn.rotate(tile.getDirection().getRotation());

        matrixStackIn.rotate(Const.X90);

        //hours
        matrixStackIn.push();

        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.interpolateAngle(partialTicks, tile.prevRoll, tile.roll)));
        matrixStackIn.translate(0,-1.5, -0.5+0.02083333);

        this.hourHand.render(matrixStackIn, builder, combinedLightIn,combinedOverlayIn,1,1,1,1);

        matrixStackIn.pop();

        //minutes
        matrixStackIn.push();

        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.interpolateAngle(partialTicks, tile.sPrevRoll, tile.sRoll)));
        matrixStackIn.translate(0,-1.5, -0.5+0.04166667);

        this.minuteHand.render(matrixStackIn, builder, combinedLightIn,combinedOverlayIn,1,1,1,1);

        matrixStackIn.pop();

        matrixStackIn.pop();
    }


}