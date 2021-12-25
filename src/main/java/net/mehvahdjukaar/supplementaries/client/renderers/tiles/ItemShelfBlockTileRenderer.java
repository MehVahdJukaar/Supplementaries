package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.common.block.tiles.ItemShelfBlockTile;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class ItemShelfBlockTileRenderer implements BlockEntityRenderer<ItemShelfBlockTile> {
    protected final ItemRenderer itemRenderer;
    private final Font font;

    public ItemShelfBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        font = context.getFont();
    }

    protected boolean canRenderName(ItemShelfBlockTile tile) {
        if (Minecraft.renderNames() && tile.getItem(0).hasCustomHoverName()) {
            double d0 = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5, tile.getBlockPos().getZ() + 0.5);
            return d0 < 16;
        }
        return false;
    }

    protected void renderName(Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        double f = 0.625; //height
        int i = 0;

        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, f, 0);
        matrixStackIn.mulPose(renderManager.cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStackIn.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int) (f1 * 255.0F) << 24;

        float f2 = (float) (-font.width(displayNameIn) / 2);
        //drawInBatch == renderTextComponent
        font.drawInBatch(displayNameIn, f2, (float) i, -1, false, matrix4f, bufferIn, false, j, packedLightIn);
        matrixStackIn.popPose();

    }


    @Override
    public void render(ItemShelfBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if (!tile.isEmpty()) {

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            float yaw = tile.getYaw();
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(yaw));
            matrixStackIn.translate(0, 0, 0.8125);

            if (this.canRenderName(tile)) {
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-yaw));
                Component name = tile.getItem(0).getHoverName();
                int i = "Dinnerbone".equals(name.getString()) ? -1 : 1;
                matrixStackIn.scale(i, i, 1);
                this.renderName(name, matrixStackIn, bufferIn, combinedLightIn);
                matrixStackIn.popPose();
            }

            ItemStack stack = tile.getDisplayedItem();
            if (CommonUtil.FESTIVITY.isAprilsFool()) stack = new ItemStack(Items.SALMON);
            BakedModel model = itemRenderer.getModel(stack, tile.getLevel(), null, 0);
            if (model.isGui3d() && ClientConfigs.cached.SHELF_TRANSLATE) matrixStackIn.translate(0, -0.25, 0);


            itemRenderer.render(stack, ItemTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                    combinedOverlayIn, model);

            matrixStackIn.popPose();
        }

    }

}