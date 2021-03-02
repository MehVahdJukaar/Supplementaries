package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Random;

import static net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer.renderFluid;


public class JarItemRenderer extends CageItemRenderer {

    private static final Random RAND = new Random(420);

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        CompoundNBT compound = stack.getChildTag("BlockEntityTag");
        if(compound == null || compound.isEmpty())return;

        JarBlockTile.SpecialJarContent specialType = JarBlockTile.SpecialJarContent.values()[compound.getInt("SpecialType")];
        if(specialType.isCookie()){
            if(compound.contains("Items")) {
                RAND.setSeed(420);
                ItemStack cookieStack = ItemStack.read((compound.getList("Items", 10)).getCompound(0));
                int height = cookieStack.getCount();
                matrixStackIn.push();
                matrixStackIn.translate(0.5, 0.5, 0.5);
                matrixStackIn.rotate(Const.XN90);
                matrixStackIn.translate(0, 0, -0.5);
                float scale = 8f / 14f;
                matrixStackIn.scale(scale, scale, scale);
                for (float i = 0; i < height; i ++) {
                    matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(RAND.nextInt(360)));
                    // matrixStackIn.translate(0, 0, 0.0625);
                    matrixStackIn.translate(0, 0, 1 / (16f * scale));
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(cookieStack, null, null);
                    itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                            combinedOverlayIn, ibakedmodel);
                }
                matrixStackIn.pop();
            }
        }
        else if(specialType.isFish()){
            matrixStackIn.push();
            IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
            matrixStackIn.translate(0.5, 0.375, 0.5);
            matrixStackIn.rotate(Const.YN45);
            // matrixStackIn.scale(0.6f, 0.6f, 0.6f);
            RendererUtil.renderFish(builder1, matrixStackIn, 0, 0, specialType.getFishTextureOffset(), combinedLightIn, combinedOverlayIn);
            matrixStackIn.pop();
            SoftFluid s = SoftFluidList.WATER;
            renderFluid(0.5625f, s.getTintColor(), 0, s.getStillTexture(),
                    matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,false);
        }
        else{
            CompoundNBT com = compound.getCompound("FluidHolder");
            if(com!=null){
                int color = com.getInt("CachedColor");
                int height = com.getInt("Count");
                SoftFluid fluid = SoftFluidList.fromID(com.getString("Fluid"));
                if(!fluid.isEmpty()&&height>0)
                renderFluid(height/16f, color, 0, fluid.getStillTexture(),
                        matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,false);
            }
        }

        //render block & mob using cage renderer
        super.func_239207_a_(stack,transformType,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn);
    }
}

