package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.client.TextUtils;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestMapDataPacket;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;


public class NoticeBoardBlockTileRenderer implements BlockEntityRenderer<NoticeBoardBlockTile> {
    private final ItemRenderer itemRenderer;
    private final MapRenderer mapRenderer;
    private final Font font;
    private final Camera camera;
    private static final float PAPER_X_MARGIN = 0.1875f;
    private static final float PAPER_Y_MARGIN = 0.125f;

    public NoticeBoardBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft minecraft = Minecraft.getInstance();
        itemRenderer = minecraft.getItemRenderer();
        mapRenderer = minecraft.gameRenderer.getMapRenderer();
        font = context.getFont();
        camera = minecraft.gameRenderer.getMainCamera();
    }

    public int getFrontLight(Level world, BlockPos pos, Direction dir) {
        return LevelRenderer.getLightColor(world, pos.relative(dir));
    }

    public boolean getAxis(Direction dir) {
        return dir == Direction.NORTH || dir == Direction.SOUTH;
    }

    @Override
    public void render(NoticeBoardBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        //TODO: rewrite
        if (!tile.shouldSkipTileRenderer()) {
            Level world = tile.getLevel();
            if (world == null) return;

            ItemStack stack = tile.getDisplayedItem();
            if (stack.isEmpty()) return;

            Direction dir = tile.getDirection();

            float yaw = -dir.toYRot();
            Vec3 cameraPos = camera.getPosition();
            BlockPos pos = tile.getBlockPos();
            if (LOD.isOutOfFocus(cameraPos, pos, yaw)) return;

            //TODO: fix book with nothing in it
            int frontLight = this.getFrontLight(world, pos, dir);

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.mulPose(RotHlpr.rot((int) yaw));
            matrixStackIn.translate(0, 0, 0.5);

            //render map
            MapItemSavedData mapData = MapItem.getSavedData(stack, world);
            if (stack.getItem() instanceof ComplexItem) {
                if (mapData != null) {
                    matrixStackIn.pushPose();
                    matrixStackIn.translate(0, 0, 0.008);
                    matrixStackIn.scale(0.0078125F, -0.0078125F, -0.0078125F);
                    matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
                    Integer integer = MapItem.getMapId(stack);
                    mapRenderer.render(matrixStackIn, bufferIn, integer, mapData, true, frontLight);
                    matrixStackIn.popPose();
                } else {
                    //request map data from server
                    Player player = Minecraft.getInstance().player;
                    NetworkHandler.CHANNEL.sendToServer(new ServerBoundRequestMapDataPacket(tile.getBlockPos(), player.getUUID()));
                }
                matrixStackIn.popPose();
                return;
            }

            //render book
            String page = tile.getText();
            if (!(page == null || page.equals(""))) {

                LOD lod = new LOD(cameraPos, pos);
                if (!lod.isNearMed()) {
                    matrixStackIn.popPose();
                    return;
                }

                matrixStackIn.pushPose();
                matrixStackIn.translate(0, 0.5, 0.008);

                float d0;
                if (this.getAxis(dir)) {
                    d0 = 0.8f * 0.7f;
                } else {
                    d0 = 0.6f * 0.7f;
                }


                if (MiscUtils.FESTIVITY.isAprilsFool()) {
                    TextUtils.renderBeeMovie(matrixStackIn, bufferIn, frontLight, font, d0);
                    matrixStackIn.popPose();
                    matrixStackIn.popPose();
                    return;
                }

                String bookName = tile.getItem(0).getHoverName().getString().toLowerCase(Locale.ROOT);
                if (bookName.equals("credits")) {
                    TextUtils.renderCredits(matrixStackIn, bufferIn, frontLight, font, d0);
                    matrixStackIn.popPose();
                    matrixStackIn.popPose();
                    return;
                }
                int i = tile.getTextColor().getTextColor();
                int r = (int) ((double) FastColor.ABGR32.red(i) * d0);
                int g = (int) ((double) FastColor.ABGR32.green(i) * d0);
                int b = (int) ((double) FastColor.ABGR32.blue(i) * d0);
                int i1 = FastColor.ABGR32.color(0, b, g, r);

                if (tile.needsVisualUpdate()) {
                    float paperWidth = 1 - (2 * PAPER_X_MARGIN);
                    float paperHeight = 1 - (2 * PAPER_Y_MARGIN);
                    var p = TextUtil.fitLinesToBox(font,
                            TextUtil.parseText(page), paperWidth, paperHeight);
                    tile.setFontScale(p.getSecond());
                    tile.setCachedPageLines(p.getFirst());
                }
                List<FormattedCharSequence> tempPageLines = tile.getCachedPageLines();

                float scale = tile.getFontScale();
                matrixStackIn.scale(scale, -scale, scale);
                int numberOfLines = tempPageLines.size();
                boolean centered = ClientConfigs.Blocks.NOTICE_BOARD_CENTERED_TEXT.get();

                for (int lin = 0; lin < numberOfLines; ++lin) {
                    FormattedCharSequence str = tempPageLines.get(lin);

                    //border offsets. always add 0.5 to center properly
                    float dx = centered ? (-font.width(str) / 2f) + 0.5f : -(0.5f - PAPER_X_MARGIN) / scale;

                    float dy = (((1f / scale) - (8 * numberOfLines)) / 2f) + 0.5f;
                    if (!bookName.equals("missingno")) {
                        font.drawInBatch(str, dx, dy + 8 * lin, i1, false, matrixStackIn.last().pose(), bufferIn, Font.DisplayMode.NORMAL, 0, frontLight);
                    } else {
                        font.drawInBatch("§ka", dx, dy + 8 * lin, i1, false, matrixStackIn.last().pose(), bufferIn, Font.DisplayMode.NORMAL, 0, frontLight);
                    }
                }
                matrixStackIn.popPose();
                matrixStackIn.popPose();
                return;
            }

            //render item

            Material pattern = tile.getCachedPattern();
            if (pattern != null) {

                VertexConsumer builder = pattern.buffer(bufferIn, RenderType::entityNoOutline);

                int i = tile.getTextColor().getTextColor();
                float b = (FastColor.ARGB32.blue(i)) / 255f;
                float g = (FastColor.ARGB32.green(i)) / 255f;
                float r = (FastColor.ARGB32.red(i)) / 255f;
                //if(tile.textHolder.hasGlowingText())combinedLightIn= LightTexture.FULL_BRIGHT;
                int lu = frontLight & '\uffff';
                int lv = frontLight >> 16 & '\uffff';
                VertexUtils.addQuadSide(builder, matrixStackIn, -0.4375F, -0.4375F, 0.008f, 0.4375F, 0.4375F, 0.008f,
                        0.15625f, 0.0625f, 0.5f + 0.09375f, 1 - 0.0625f, r, g, b, 1, lu, lv, 0, 0, 1, pattern.sprite());

            } else if (!tile.isNormalItem()) {
                BakedModel model = itemRenderer.getModel(stack, world, null, 0);

                matrixStackIn.translate(0, 0, 0.015625 + 0.00005);
                matrixStackIn.scale(-0.5f, 0.5f, -0.5f);
                itemRenderer.render(stack, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn, frontLight,
                        combinedOverlayIn, model);
                //itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, newl, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
            }
            matrixStackIn.popPose();
        }
    }

}