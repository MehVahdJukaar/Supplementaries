package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.LazyModelPart;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer.renderBanner;


public class FlagItemRenderer extends ItemStackRenderer {

    private final BlockState state = ModRegistry.FLAGS.get(DyeColor.BLACK).get().defaultBlockState();
    private final LazyModelPart flag = LazyModelPart.of(ModelLayers.BANNER, "flag");

    public FlagItemRenderer() {
        super();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(-0.71875, 0, 0);

        CompoundTag com = stack.getTagElement("BlockEntityTag");
        ListTag listnbt = null;
        if (com != null && com.contains("Patterns")) {
            listnbt = com.getList("Patterns", 10);
        }
        DyeColor color = ((FlagItem) stack.getItem()).getColor();
        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = BannerBlockEntity.createPatterns(color, listnbt);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0.5 + 0.0625, 0, 0.5);

        if (ClientConfigs.Blocks.FLAG_BANNER.get()) {
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(100));
            renderBanner(flag.get(), 0, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, patterns);

        } else {
            matrixStackIn.mulPose(RotHlpr.Y90);
            FlagBlockTileRenderer.renderPatterns(matrixStackIn, bufferIn, patterns, combinedLightIn);
        }
        matrixStackIn.popPose();

    }
}