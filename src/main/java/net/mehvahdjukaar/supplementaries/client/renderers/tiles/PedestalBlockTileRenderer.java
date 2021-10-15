package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.block.blocks.PedestalBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.PedestalBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Direction;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.Component;


public class PedestalBlockTileRenderer extends BlockEntityRenderer<PedestalBlockTile> {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher entityRenderer;

    public PedestalBlockTileRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        ;
        itemRenderer = minecraft.getItemRenderer();
        entityRenderer = minecraft.getEntityRenderDispatcher();
    }

    protected boolean canRenderName(PedestalBlockTile tile) {
        if (Minecraft.renderNames() && tile.getItem(0).hasCustomHoverName()) {
            double d0 = entityRenderer.distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5, tile.getBlockPos().getZ() + 0.5);
            return d0 < 16 * 16;
        }
        return false;
    }

    protected void renderName(Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        double f = 0.875; //height
        int i = 0;

        Font fontrenderer = this.renderer.getFont();

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, f, 0);
        matrixStackIn.mulPose(entityRenderer.cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStackIn.last().pose();
        float f1 = minecraft.options.getBackgroundOpacity(0.25F);
        int j = (int) (f1 * 255.0F) << 24;

        float f2 = (float) (-fontrenderer.width(displayNameIn) / 2);
        //drawInBatch == renderTextComponent
        fontrenderer.drawInBatch(displayNameIn, f2, (float) i, -1, false, matrix4f, bufferIn, false, j, packedLightIn);
        matrixStackIn.popPose();

    }


    @Override
    public void render(PedestalBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if (!tile.isEmpty()) {
            //TODO: optimize transforms
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 1.125, 0.5);

            if (this.canRenderName(tile)) {
                Component name = tile.getItem(0).getHoverName();
                int i = "Dinnerbone".equals(name.getString()) ? -1 : 1;
                matrixStackIn.scale(i, i, 1);
                this.renderName(name, matrixStackIn, bufferIn, combinedLightIn);
            }
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            matrixStackIn.translate(0, 0.25, 0);

            if (tile.getBlockState().getValue(PedestalBlock.AXIS) == Direction.Axis.X) {
                matrixStackIn.mulPose(Const.Y90);
            }


            ItemTransforms.TransformType transform = ItemTransforms.TransformType.FIXED;
            boolean fancy = ClientConfigs.cached.PEDESTAL_SPECIAL;
            if (tile.type == PedestalBlockTile.DisplayType.SWORD && fancy) {
                //sword
                //matrixStackIn.translate(0,-0.03125,0);
                matrixStackIn.translate(0, -0.03125, 0);
                matrixStackIn.scale(1.5f, 1.5f, 1.5f);
                matrixStackIn.mulPose(Const.Z135);
            } else if (tile.type == PedestalBlockTile.DisplayType.TRIDENT && fancy) {
                //transform = ItemCameraTransforms.TransformType.HEAD;
                //matrixStackIn.scale(1.75f,1.75f,1.75f);
                matrixStackIn.translate(0, 0.03125, 0);
                matrixStackIn.scale(1.5f, 1.5f, 1.5f);
                matrixStackIn.mulPose(Const.ZN45);
            } else if (tile.type == PedestalBlockTile.DisplayType.CRYSTAL && fancy) {
                entityRenderer.render(CapturedMobCache.pedestalCrystal.get(), 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);
                matrixStackIn.popPose();
                return;
            } else if (ClientConfigs.cached.PEDESTAL_SPIN && !minecraft.isPaused()) {
                matrixStackIn.translate(0, 6 / 16f, 0);
                matrixStackIn.scale(1.5f, 1.5f, 1.5f);

                //BlockPos blockpos = tile.getPos();
                //long blockoffset = (long) (blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13);

                //long time = System.currentTimeMillis();
                //TODO: fix stuttering
                //float tt = tile.getWorld().getGameTime()+ partialTicks;
                float tt = tile.counter + partialTicks;


                //long t = blockoffset + time;
                float angle = (tt * (float) ClientConfigs.cached.PEDESTAL_SPEED) % 360f;
                Quaternion rotation = Vector3f.YP.rotationDegrees(angle);

                //matrixStackIn.scale(1,1,0.f);
                //matrixStackIn.rotate(Const.XN22);
                //matrixStackIn.rotate(Const.Y45);

                matrixStackIn.mulPose(rotation);
            }


            //TODO: make FIXED
            ItemStack stack = tile.getDisplayedItem();
            if (CommonUtil.FESTIVITY.isAprilsFool()) stack = new ItemStack(Items.DIRT);
            itemRenderer.renderStatic(stack, transform, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            //IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);
            //itemRenderer.render(stack, transform, true, matrixStackIn, bufferIn, combinedLightIn,combinedOverlayIn, ibakedmodel);

            matrixStackIn.popPose();
        }
    }
}