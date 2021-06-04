package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;


public class FlagItemRenderer extends ItemStackTileEntityRenderer {
    private static final BlockState state = Registry.FLAGS.get(DyeColor.BLACK).get().defaultBlockState();

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(-0.71875,0,0);

        Minecraft.getInstance().getBlockRenderer().renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        CompoundNBT com = stack.getTagElement("BlockEntityTag");
        ListNBT listnbt = null;
        if (com != null && com.contains("Patterns")) {
            listnbt = com.getList("Patterns", 10);
        }
        List<Pair<BannerPattern, DyeColor>> patterns = BannerTileEntity.createPatterns(((FlagItem)stack.getItem()).getColor(), listnbt);
        matrixStackIn.translate(0.5+0.0625,0,0.5);
        matrixStackIn.mulPose(Const.Y90);
        FlagBlockTileRenderer.renderPatterns(matrixStackIn, bufferIn, patterns, combinedLightIn);

        matrixStackIn.popPose();

    }
}