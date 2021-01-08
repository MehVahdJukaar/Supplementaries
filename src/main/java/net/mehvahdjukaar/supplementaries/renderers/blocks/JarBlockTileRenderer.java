package net.mehvahdjukaar.supplementaries.renderers.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarLiquidType;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.renderers.Const;
import net.mehvahdjukaar.supplementaries.renderers.RendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class JarBlockTileRenderer extends TileEntityRenderer<JarBlockTile> {


    public JarBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(JarBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        float height = tile.liquidLevel;
        long r = tile.getPos().toLong();
        Random rand = new Random(r);
        //render cookies
        if (tile.liquidType == JarLiquidType.COOKIES) {
            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.rotate(Const.XN90);
            matrixStackIn.translate(0, 0, -0.5);
            float scale = 8f / 14f;
            matrixStackIn.scale(scale, scale, scale);
            for (float i = 0; i < height; i += 0.0625) {
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
                // matrixStackIn.translate(0, 0, 0.0625);
                matrixStackIn.translate(0, 0, 1 / (16f * scale));
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                ItemStack stack = new ItemStack(Items.COOKIE);
                IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, tile.getWorld(), null);
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                        combinedOverlayIn, ibakedmodel);
            }
            matrixStackIn.pop();
        }
        //render liquid
        else {
            //render fish
            if (tile.liquidType.isFish()) {
                matrixStackIn.push();
                IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
                long time = System.currentTimeMillis() + r;
                //TODO: fix integer division. maybe but this in tick()
                float angle =(time%(360*80))/80f;
                float angle2 = (time%(360*3))/3f;
                float angle3 = (time%(360*350))/350f;
                float wo = 0.015f * MathHelper.sin((float)(2 * Math.PI * angle2 / 360));
                float ho = 0.1f * MathHelper.sin((float)(2 * Math.PI * angle3 / 360));
                matrixStackIn.translate(0.5, 0.0635, 0.5);
                TextureAtlasSprite sprite_s = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(Resources.SAND_TEXTURE);
                RendererUtil.addCube(builder1, matrixStackIn, 0.499f, 0.0625f, sprite_s, combinedLightIn, 16777215, 1f, combinedOverlayIn, true, true,true, true);
                matrixStackIn.translate(0, 0.5-0.0635, 0);
                Quaternion rotation = Vector3f.YP.rotationDegrees(-angle);
                matrixStackIn.rotate(rotation);
                matrixStackIn.scale(0.6f, 0.6f, 0.6f);
                matrixStackIn.translate(0, -0.2, -0.35);
                RendererUtil.renderFish(builder1, matrixStackIn, wo, ho, tile.liquidType.fishType, combinedLightIn, combinedOverlayIn);
                matrixStackIn.pop();

            }
            if (height != 0) {
                matrixStackIn.push();
                int color = tile.liquidType.applyColor ? tile.color : 0xFFFFFF;
                if (color == -1) color = tile.updateClientWaterColor(); //TODO: rewrite this
                float opacity = tile.liquidType.opacity;
                ResourceLocation texture = tile.liquidType.texture;
                TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
                // TODO:remove breaking animation
                IVertexBuilder builder = bufferIn.getBuffer(
                        RenderType.getTranslucentMovingBlock());
                matrixStackIn.translate(0.5, 0.0625, 0.5);
                RendererUtil.addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true, true,
                        true, true);
                matrixStackIn.pop();
            }
        }

        Entity mob = tile.mob;
        if(mob!=null) {
            matrixStackIn.push();
            float y = tile.yOffset + MathHelper.lerp(partialTicks,tile.prevJumpY,tile.jumpY);
            float s = tile.scale;

            matrixStackIn.translate(0.5, y,0.5);
            matrixStackIn.rotate(tile.getDirection().getRotation());
            matrixStackIn.rotate(Const.XN90);
            matrixStackIn.scale(s,s,s);
            Minecraft.getInstance().getRenderManager().renderEntityStatic(mob, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);
            matrixStackIn.pop();
        }


    }
}

