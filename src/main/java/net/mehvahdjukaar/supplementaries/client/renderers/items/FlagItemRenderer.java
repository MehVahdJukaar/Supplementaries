package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;


public class FlagItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final BlockState state = ModRegistry.FLAGS.get(DyeColor.BLACK).get().defaultBlockState();

    public FlagItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(-0.71875, 0, 0);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        CompoundTag com = stack.getTagElement("BlockEntityTag");
        ListTag listnbt = null;
        if (com != null && com.contains("Patterns")) {
            listnbt = com.getList("Patterns", 10);
        }
        List<Pair<BannerPattern, DyeColor>> patterns = BannerBlockEntity.createPatterns(((FlagItem) stack.getItem()).getColor(), listnbt);
        matrixStackIn.translate(0.5 + 0.0625, 0, 0.5);
        matrixStackIn.mulPose(RotHlpr.Y90);
        FlagBlockTileRenderer.renderPatterns(matrixStackIn, bufferIn, patterns, combinedLightIn);

        matrixStackIn.popPose();

    }
}