package net.mehvahdjukaar.supplementaries.renderers;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Optional;


@OnlyIn(Dist.CLIENT)
public class MobJarItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {



        matrixStackIn.push();
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = Registry.FIREFLY_JAR.getDefaultState();
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

        CompoundNBT compound = stack.getTag();
        if(compound != null && compound.contains("CachedJarMobValues") && compound.contains("JarMob")) {
            CompoundNBT cmp = compound.getCompound("JarMob");
            CompoundNBT cmp2 = compound.getCompound("CachedJarMobValues");

            EntityType type = net.minecraft.util.registry.Registry.ENTITY_TYPE.getOrDefault(new ResourceLocation(cmp.getString("id")));

            Entity e = type.create(Minecraft.getInstance().world);
            if (e != null) {
                float y = cmp2.getFloat("YOffset");
                float s = cmp2.getFloat("Scale");

                matrixStackIn.translate(0.5, y, 0.5);
                matrixStackIn.scale(s, s, s);
                Minecraft.getInstance().getRenderManager().renderEntityStatic(e, 0.0D, 0.0D, 0.0D, 0.0F, 0, matrixStackIn, bufferIn, combinedLightIn);
            }
        }
        matrixStackIn.pop();
    }
}