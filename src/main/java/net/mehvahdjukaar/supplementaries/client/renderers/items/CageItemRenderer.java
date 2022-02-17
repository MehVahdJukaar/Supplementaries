package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CageBlockTileRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.UUID;


public class CageItemRenderer extends BlockEntityWithoutLevelRenderer {

    public CageItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    public void renderTileStuff(CompoundTag tag, ItemTransforms.TransformType transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        //render mob
        if (tag.contains("MobHolder")) {
            CompoundTag cmp2 = tag.getCompound("MobHolder");
            if (cmp2.contains("FishTexture")) return;
            if (cmp2.contains("UUID")) {
                UUID id = cmp2.getUUID("UUID");
                Entity e = CapturedMobCache.getOrCreateCachedMob(id, cmp2);

                if (e != null) {
                    float s = cmp2.getFloat("Scale");
                    matrixStackIn.pushPose();

                    EntityRenderDispatcher entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();

                            matrixStackIn.translate(0.5, 0.5, 0.5);
                            matrixStackIn.mulPose(RotHlpr.Y180);
                            matrixStackIn.translate(-0.5, -0.5, -0.5);


                    CageBlockTileRenderer.renderMobStatic(e, s, entityRenderer, matrixStackIn, 1, bufferIn, combinedLightIn, -90);

                    matrixStackIn.popPose();
                }
            }
        }

    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        //render block
        matrixStackIn.pushPose();
        BlockItem item = ((BlockItem) stack.getItem());
        BlockState state = item.getBlock().defaultBlockState();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.popPose();

        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag != null) {
            this.renderTileStuff(tag, transformType, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }

    }
}

