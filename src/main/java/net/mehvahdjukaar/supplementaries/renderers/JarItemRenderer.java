package net.mehvahdjukaar.supplementaries.renderers;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.JarContentType;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;


@OnlyIn(Dist.CLIENT)
public class JarItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = Registry.JAR.get().getDefaultState();
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();
        float height = 0;
        int color = 0xffffff;
        JarContentType lt = JarContentType.EMPTY;
        CompoundNBT compound = stack.getTag();
        if (compound != null && !compound.isEmpty() && compound.contains("BlockEntityTag")) {
            compound = compound.getCompound("BlockEntityTag");
            if (compound.contains("liquidType")) {
                lt = JarContentType.values()[compound.getInt("liquidType")];
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
            if (lt == JarContentType.COOKIES) {
                matrixStackIn.push();
                matrixStackIn.translate(0.5, 0.5, 0.5);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
                matrixStackIn.translate(0, 0, -0.5);
                float scale = 8f / 14f;
                matrixStackIn.scale(scale, scale, scale);
                for (float i = 0; i < height; i += 0.0625) {
                    matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
                    matrixStackIn.translate(0, 0, 0.0625);
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    ItemStack itemstack = new ItemStack(Items.COOKIE);
                    IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(itemstack, null, null);
                    itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                            combinedOverlayIn, ibakedmodel);
                    matrixStackIn.translate(0, 0, scale / 16f);
                }
                matrixStackIn.pop();
            }
            //liquid
            else {
                //fish
                if (lt.isFish()) {
                    matrixStackIn.push();
                    IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
                    matrixStackIn.translate(0.5, 0.375, 0.5);
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-45));
                    // matrixStackIn.scale(0.6f, 0.6f, 0.6f);
                    RendererUtil.renderFish(builder1, matrixStackIn, 0, 0, lt.fishType, combinedLightIn, combinedOverlayIn);
                    matrixStackIn.pop();
                }
                if (height != -0) {
                    matrixStackIn.push();
                    TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(t);
                    IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucentMovingBlock());
                    matrixStackIn.translate(0.5, 0.0625, 0.5);
                    RendererUtil.addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true,
                            true, false, true);
                    matrixStackIn.pop();
                }
            }
        }
    }
}

