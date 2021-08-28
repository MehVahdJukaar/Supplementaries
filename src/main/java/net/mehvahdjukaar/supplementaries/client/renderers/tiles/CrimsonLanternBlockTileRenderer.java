package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.OilLanternBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class CrimsonLanternBlockTileRenderer extends EnhancedLanternBlockTileRenderer<OilLanternBlockTile> {

    public static final RenderMaterial GOLD_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, Textures.CRIMSON_LANTERN_TEXTURE);
    private final ModelRenderer gold = new ModelRenderer(16, 16, 0, 0);
    public CrimsonLanternBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

        gold.setPos(0.0F, -4.0F, 0.0F);
        gold.addBox(-2.0F, 0.0F, 2.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);

        ModelRenderer cube_r1 = new ModelRenderer(16, 16, 0, 0);
        cube_r1.setPos(0.0F, 0.0F, 0.0F);
        gold.addChild(cube_r1);
        cube_r1.yRot=1.5708F;
        cube_r1.addBox(-2.0F, 0.0F, 2.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);

        ModelRenderer cube_r2 = new ModelRenderer(16, 16, 0, 0);
        cube_r2.setPos(0.0F, 0.0F, 0.0F);
        gold.addChild(cube_r2);
        cube_r2.yRot=3.1416F;
        cube_r2.addBox(-2.0F, 0.0F, 2.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);

        ModelRenderer cube_r3 = new ModelRenderer(16, 16, 0, 0);
        cube_r3.setPos(0.0F, 0.0F, 0.0F);
        gold.addChild(cube_r3);
        cube_r3.yRot=-1.5708F;
        cube_r3.addBox(-2.0F, 0.0F, 2.0F, 4.0F, 5.0F, 0.0F, 0.0F, false);

    }

    @Override
    public void render(OilLanternBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        AttachFace face = tile.getBlockState().getValue(CopperLanternBlock.FACE);
        if(face==AttachFace.FLOOR)return;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0,-0.0625,0);
        BlockState state = tile.getBlockState().getBlock().defaultBlockState();


        matrixStackIn.pushPose();
        // rotate towards direction
        matrixStackIn.translate(0.5, 0.875, 0.5);


        matrixStackIn.mulPose(Const.rot(tile.getDirection().getOpposite()));
        matrixStackIn.mulPose(Const.XN90);


        // animation
        if(face==AttachFace.CEILING) {
            float yrot = MathHelper.lerp(partialTicks, tile.prevAngle * 1.5f, tile.angle * 1.5f);
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(yrot));
            matrixStackIn.translate(-0.5, -0.5625, -0.5);



            RendererUtil.renderBlockModel(state, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos(), RenderType.cutout());


            IVertexBuilder builder = GOLD_TEXTURE.buffer(bufferIn, RenderType::entityCutoutNoCull);
            matrixStackIn.translate(0.5, 0, 0.5);

            this.gold.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        }
        else {
            float xrot = MathHelper.lerp(partialTicks, tile.prevAngle, tile.angle);
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(xrot));
            matrixStackIn.translate(-0.5, -0.75, -0.375);

            RendererUtil.renderBlockModel(state, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());


            IVertexBuilder builder = GOLD_TEXTURE.buffer(bufferIn, RenderType::entityCutoutNoCull);
            matrixStackIn.translate(0.5, 0, 0.5);
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-xrot*0.75f));
            this.gold.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        }
        // render block

        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);


        matrixStackIn.popPose();


        matrixStackIn.popPose();

    }
}