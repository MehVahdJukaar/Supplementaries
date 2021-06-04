package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
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
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        //render block
        matrixStackIn.pushPose();
        BlockState state = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
        Minecraft.getInstance().getBlockRenderer().renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.popPose();

        CompoundNBT compound = stack.getTag();
        if(compound == null || compound.isEmpty())return;

        //render mob
        if(compound.contains("BlockEntityTag")) {
            CompoundNBT cmp = compound.getCompound("BlockEntityTag");
            if (cmp.contains("MobHolder")) {
                CompoundNBT cmp2 = cmp.getCompound("MobHolder");
                if(cmp2.contains("FishTexture"))return;
                if (cmp2.contains("UUID")) {
                    UUID id = cmp2.getUUID("UUID");
                    Entity e = CapturedMobCache.getCachedMob(id);

                    if (e == null) {
                        World world = Minecraft.getInstance().level;
                        if(world != null) {
                            CompoundNBT mobData = cmp2.getCompound("EntityData");

                            //TODO:
                            e = MobHolder.createEntityFromNBT(mobData,id,world);
                            CapturedMobCache.addMob(e);
                        }
                    }
                    if (e != null) {
                        float y = cmp2.getFloat("YOffset");
                        float s = cmp2.getFloat("Scale");
                        matrixStackIn.pushPose();
                        matrixStackIn.translate(0.5, y, 0.5);
                        matrixStackIn.scale(-s, s, -s);
                        Minecraft.getInstance().getEntityRenderDispatcher().render(e, 0.0D, 0.0D, 0.0D, 0.0F, 0, matrixStackIn, bufferIn, combinedLightIn);
                        matrixStackIn.popPose();
                    }
                }
            }
        }
    }
}

