package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.GlobeRenderData;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PedestalBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PedestalBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;


public class PedestalBlockTileRenderer implements BlockEntityRenderer<PedestalBlockTile> {
    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher entityRenderer;

    public PedestalBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.entityRenderer = minecraft.getEntityRenderDispatcher();
    }

    @ForgeOverride
    public AABB getRenderBoundingBox(BlockEntity tile) {
        return new AABB(tile.getBlockPos()).expandTowards(0,1,0);
    }

    protected boolean canRenderName(ItemStack item, PedestalBlockTile tile, PedestalBlockTile.DisplayType type) {
        if (Minecraft.renderNames() && item.has(DataComponents.CUSTOM_NAME) && !type.isGlobe()) {
            double d0 = entityRenderer.distanceToSqr(tile.getBlockPos().getX() + 0.5, tile.getBlockPos().getY() + 0.5, tile.getBlockPos().getZ() + 0.5);
            return d0 < 16 * 16;
        }
        return false;
    }

    public static void renderName(Component name, float h, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn) {
        Minecraft mc = Minecraft.getInstance();

        int s = "Dinnerbone".equals(name.getString()) ? -1 : 1;
        poseStack.scale(s, s, 1);

        int i = 0;
        poseStack.pushPose();

        poseStack.translate(0, h, 0);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, -0.025F);
        Matrix4f matrix4f = poseStack.last().pose();
        float f1 = mc.options.getBackgroundOpacity(0.25F);
        int j = (int) (f1 * 255.0F) << 24;

        float f2 = (-mc.font.width(name) / 2f);

        mc.font.drawInBatch(name, f2, i, -1, false, matrix4f, bufferIn, Font.DisplayMode.NORMAL, j, combinedLightIn);
        poseStack.popPose();
    }

    @Override
    public void render(PedestalBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (!tile.isEmpty()) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 1.125, 0.5);

            var displayType = tile.getDisplayType();
            ItemStack stack = tile.getDisplayedItem();

            if (this.canRenderName(stack, tile, displayType)) {
                renderName(tile.getItem(0).getHoverName(), 0.875f, matrixStackIn, bufferIn, combinedLightIn);
            }
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            matrixStackIn.translate(0, 0.25, 0);

            if (tile.getBlockState().getValue(PedestalBlock.AXIS) == Direction.Axis.X) {
                matrixStackIn.mulPose(RotHlpr.Y90);
            }

            ItemDisplayContext transform = ItemDisplayContext.FIXED;

            if (ClientConfigs.Blocks.PEDESTAL_SPECIAL.get()) {
                switch (displayType) {
                    case SWORD -> {
                        matrixStackIn.translate(0, -0.03125, 0);
                        matrixStackIn.scale(1.5f, 1.5f, 1.5f);
                        matrixStackIn.mulPose(RotHlpr.Z135);
                    }
                    case TRIDENT -> {
                        matrixStackIn.translate(0, 0.03125, 0);
                        matrixStackIn.scale(1.5f, 1.5f, 1.5f);
                        matrixStackIn.mulPose(RotHlpr.ZN45);
                    }
                    case CRYSTAL -> {
                        entityRenderer.render(CapturedMobCache.getEndCrystal(tile.getLevel()), 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);
                        matrixStackIn.popPose();
                        return;
                    }
                    default -> {

                        if (ClientConfigs.Blocks.PEDESTAL_SPIN.get()) {
                            matrixStackIn.translate(0, 6 / 16f, 0);
                            matrixStackIn.scale(1.5f, 1.5f, 1.5f);

                            //BlockPos blockpos = tile.getPos();
                            //long blockoffset = (long) (blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13);

                            //long time = System.currentTimeMillis();

                            //float tt = tile.getLevel().getGameTime() +partialTicks;
                            //float tt = tile.counter + partialTicks;

                            //float tt = ((float)Math.floorMod(tile.getLevel().getGameTime(), 1000L) + partialTicks) / 1000.0F;

                            //long t = blockoffset + time;

                            int scale = (int) (ClientConfigs.Blocks.PEDESTAL_SPEED.get() * 360f);
                            long time = tile.getLevel().getGameTime();
                            float angle = (Math.floorMod(time, (long) scale) + partialTicks) / (float) scale;
                            Quaternionf rotation = Axis.YP.rotation((float) (angle * Math.PI * 10));

                            matrixStackIn.mulPose(rotation);
                        }

                        if (displayType.isGlobe()) {
                            if (GlobeBlockTileRenderer.INSTANCE != null) {

                                boolean sepia = tile.getDisplayType() == PedestalBlockTile.DisplayType.SEPIA_GLOBE;
                                GlobeRenderData data = GlobeManager.computeRenderData(false, stack.get(DataComponents.CUSTOM_NAME));

                                GlobeBlockTileRenderer.INSTANCE.renderGlobe(data, matrixStackIn, bufferIn,
                                        combinedLightIn, combinedOverlayIn, sepia, tile.getLevel());
                            }
                            matrixStackIn.popPose();
                            return;
                        }
                    }
                }
            }


            if (MiscUtils.FESTIVITY.isAprilsFool()) stack = new ItemStack(Items.DIRT);
            this.itemRenderer.renderStatic(stack, transform, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, tile.getLevel(), 0);

            matrixStackIn.popPose();
        }
    }


}