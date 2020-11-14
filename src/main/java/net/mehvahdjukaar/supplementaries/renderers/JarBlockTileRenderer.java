package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarContentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;

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
        if (tile.liquidType == JarContentType.COOKIES) {
            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
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
                long time = System.currentTimeMillis();
                //TODO: fix integer division. maybe but this in tick()
                float angle =((time / 80) + r) % 360;
                float angle2 = ((time / 3) + r) % 360;
                float angle3 = ((time / 350) + r) % 360;
                float wo = 0.015f * MathHelper.sin((float)(2 * Math.PI * angle2 / 360));
                float ho = 0.1f * MathHelper.sin((float)(2 * Math.PI * angle3 / 360));
                matrixStackIn.translate(0.5, 0.5, 0.5);
                Quaternion rotation = Vector3f.YP.rotationDegrees(-angle);
                matrixStackIn.rotate(rotation);
                matrixStackIn.scale(0.6f, 0.6f, 0.6f);
                matrixStackIn.translate(0, -0.2, -0.35);
                CommonUtil.renderFish(builder1, matrixStackIn, wo, ho, tile.liquidType.fishType, combinedLightIn, combinedOverlayIn);
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
                CommonUtil.addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true, true,
                        true, true);
                matrixStackIn.pop();
            }
        }
    }
}

