package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.common.CagedMobHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.UUID;


public class CageItemRenderer extends ItemStackTileEntityRenderer {

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        //render block
        matrixStackIn.push();
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();

        CompoundNBT compound = stack.getTag();
        if(compound == null || compound.isEmpty())return;

        //render mob
        if(compound.contains("BlockEntityTag")) {
            CompoundNBT cmp = compound.getCompound("BlockEntityTag");
            if (cmp.contains("MobHolder")) {
                CompoundNBT cmp2 = cmp.getCompound("MobHolder");

                if (cmp2.contains("UUID")) {
                    UUID id = cmp2.getUniqueId("UUID");
                    Entity e = CagedMobHelper.getCachedMob(id);

                    if (e == null) {
                        World world = Minecraft.getInstance().world;
                        if(world != null) {
                            CompoundNBT mobData = cmp2.getCompound("EntityData");

                            e = MobHolder.createEntityFromNBT(mobData,id,world);
                            CagedMobHelper.addMob(e);
                        }
                    }
                    if (e != null) {
                        float y = cmp2.getFloat("YOffset");
                        float s = cmp2.getFloat("Scale");
                        matrixStackIn.push();
                        matrixStackIn.translate(0.5, y, 0.5);
                        matrixStackIn.scale(-s, s, -s);
                        Minecraft.getInstance().getRenderManager().renderEntityStatic(e, 0.0D, 0.0D, 0.0D, 0.0F, 0, matrixStackIn, bufferIn, combinedLightIn);
                        matrixStackIn.pop();
                    }
                }
            }
        }
    }
}

