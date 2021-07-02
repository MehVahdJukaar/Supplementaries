package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Random;


public class JarBlockTileRenderer extends CageBlockTileRenderer<JarBlockTile> {
    private final ItemRenderer itemRenderer;
    private final Minecraft minecraft = Minecraft.getInstance();

    public JarBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        itemRenderer = minecraft.getItemRenderer();
    }

    public static void renderFluid(float height, int color, int luminosity, ResourceLocation texture, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int light, int combinedOverlayIn, boolean shading){
        matrixStackIn.pushPose();
        float opacity = 1;//tile.liquidType.opacity;
        if(luminosity!=0) light = light & 15728640 | luminosity << 4;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(texture);
        // TODO:remove breaking animation
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.translucentMovingBlock());
        matrixStackIn.translate(0.5, 0.0625, 0.5);
        RendererUtil.addCube(builder, matrixStackIn, 0.5f, height, sprite, light, color, opacity, combinedOverlayIn, true, true, shading, true);
        matrixStackIn.popPose();
    }


    @Override
    public void render(JarBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        long r = tile.getBlockPos().asLong();
        Random rand = new Random(r);
        //render cookies
        if(!tile.isEmpty()){
            ItemStack stack = tile.getDisplayedItem();
            int height = tile.getDisplayedItem().getCount();
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.mulPose(Const.XN90);
            matrixStackIn.translate(0, 0, -0.5);
            float scale = 8f / 14f;
            matrixStackIn.scale(scale, scale, scale);
            for (float i = 0; i < height; i ++) {
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
                // matrixStackIn.translate(0, 0, 0.0625);
                matrixStackIn.translate(0, 0, 1 / (16f * scale));
                IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);
                itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                        combinedOverlayIn, ibakedmodel);
            }
            matrixStackIn.popPose();
        }
        //render fish
        if(!tile.mobHolder.isEmpty()) {
            if (tile.mobHolder.capturedMobProperties.isFish()) {
                matrixStackIn.pushPose();

                long time = System.currentTimeMillis() + r;
                float angle = (time % (360 * 80)) / 80f;
                float angle2 = (time % (360 * 3)) / 3f;
                float angle3 = (time % (360 * 350)) / 350f;
                float wo = 0.015f * MathHelper.sin((float) (2 * Math.PI * angle2 / 360));
                float ho = 0.1f * MathHelper.sin((float) (2 * Math.PI * angle3 / 360));
                IVertexBuilder builder = bufferIn.getBuffer(RenderType.cutout());
                matrixStackIn.translate(0.5, 0.5, 0.5);
                Quaternion rotation = Vector3f.YP.rotationDegrees(-angle);
                matrixStackIn.mulPose(rotation);
                matrixStackIn.scale(0.625f, 0.625f, 0.625f);
                matrixStackIn.translate(0, -0.2, -0.335);
                int fishType = tile.mobHolder.capturedMobProperties.getFishTexture();

                //overlay
                RendererUtil.renderFish(builder, matrixStackIn, wo, ho, fishType, combinedLightIn, combinedOverlayIn);
                matrixStackIn.popPose();

            }
            else {
                super.render(tile, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
            }
            if (tile.mobHolder.shouldHaveWater()) {
                matrixStackIn.pushPose();
                matrixStackIn.translate(0.5, 0.0635, 0.5);
                IVertexBuilder builder = bufferIn.getBuffer(RenderType.cutout());
                TextureAtlasSprite sprite_s = minecraft.getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.SAND_TEXTURE);
                RendererUtil.addCube(builder, matrixStackIn, 0.499f, 0.0625f, sprite_s, combinedLightIn, 16777215, 1f, combinedOverlayIn, true, true, true, true);
                matrixStackIn.popPose();
                matrixStackIn.pushPose();
                SoftFluid s = SoftFluidRegistry.WATER;
                renderFluid(0.5625f, s.getTintColor(), 0, s.getStillTexture(),
                        matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, true);
                matrixStackIn.popPose();
            }
        }
        //render fluid
        if(!tile.fluidHolder.isEmpty()){
            renderFluid(tile.fluidHolder.getHeight(0.75f), tile.fluidHolder.getTintColor(tile.getLevel(),tile.getBlockPos()),
                    tile.fluidHolder.getFluid().getLuminosity(), tile.fluidHolder.getFluid().getStillTexture(),
                    matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,true);
        }
    }
}

