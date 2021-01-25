package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarLiquidType;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Random;



public class JarItemRenderer extends CageItemRenderer {


    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        CompoundNBT compound = stack.getTag();
        if(compound == null || compound.isEmpty())return;

        //render liquid
        float height = 0;
        int color = 0xffffff;
        JarLiquidType lt = JarLiquidType.EMPTY;
        if (compound.contains("BlockEntityTag")) {
            compound = compound.getCompound("BlockEntityTag");
            if (compound.contains("liquidType")) {
                lt = JarLiquidType.values()[compound.getInt("liquidType")];
            }
            if (compound.contains("liquidLevel"))
                height = compound.getFloat("liquidLevel");
            if (compound.contains("liquidColor") && lt.applyColor) {
                color = compound.getInt("liquidColor");
            }
            float opacity = lt.opacity;
            ResourceLocation t = lt.texture;

            Random rand = new Random(420);
            //cookies
            if (lt == JarLiquidType.COOKIES) {
                if(compound.contains("Items")) {
                    ItemStack itemStack = ItemStack.read((compound.getList("Items", 10)).getCompound(0));
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(itemStack, null, null);
                    matrixStackIn.push();
                    matrixStackIn.translate(0.5, 0.5, 0.5);
                    matrixStackIn.rotate(Const.XN90);
                    matrixStackIn.translate(0, 0, -0.5);
                    float scale = 8f / 14f;
                    matrixStackIn.scale(scale, scale, scale);
                    for (float i = 0; i < height; i += 0.0625) {
                        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
                        matrixStackIn.translate(0, 0, 0.0625);
                        itemRenderer.renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                                combinedOverlayIn, ibakedmodel);
                        matrixStackIn.translate(0, 0, scale / 16f);
                    }
                    matrixStackIn.pop();
                }

            }
            //liquid
            else {
                //fish
                if (lt.isFish()) {
                    matrixStackIn.push();
                    IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
                    matrixStackIn.translate(0.5, 0.375, 0.5);
                    matrixStackIn.rotate(Const.YN45);
                    // matrixStackIn.scale(0.6f, 0.6f, 0.6f);
                    RendererUtil.renderFish(builder1, matrixStackIn, 0, 0, lt.fishType, combinedLightIn, combinedOverlayIn);
                    matrixStackIn.pop();
                }
                if (height != -0) {
                    matrixStackIn.push();
                    TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(t);
                    IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucentMovingBlock()); //Atlases.getItemEntityTranslucentCullType()
                    matrixStackIn.translate(0.5, 0.0625, 0.5);
                    RendererUtil.addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true,
                            true, false, true);
                    matrixStackIn.pop();
                }
            }
        }

        //render block & mob using cage renderer
        super.func_239207_a_(stack,transformType,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn);


    }
}

