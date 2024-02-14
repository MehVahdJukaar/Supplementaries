package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.supplementaries.client.TextUtils;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import org.joml.Matrix4f;

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

    @Override
    public void render(NoticeBoardBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLightIn, int overlay) {

        if (!tile.shouldSkipTileRenderer()) {
            Level level = tile.getLevel();
            if (level == null) return;

            ItemStack stack = tile.getDisplayedItem();
            if (stack.isEmpty()) return;

            Direction dir = tile.getDirection();

            float yaw = -dir.toYRot();
            Vec3 cameraPos = camera.getPosition();
            BlockPos pos = tile.getBlockPos();
            if (LOD.isOutOfFocus(cameraPos, pos, yaw, 0, dir, 0)) return;

            int frontLight = this.getFrontLight(level, pos, dir);

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(RotHlpr.rot((int) yaw));
            poseStack.translate(0, 0, 0.5);


            renderNoticeBoardContent(mapRenderer, font, itemRenderer, tile, poseStack, buffer, frontLight, overlay,
                    stack, dir,
                    new LOD(cameraPos, pos));


            poseStack.popPose();
        }
    }


    public static void renderNoticeBoardContent(MapRenderer mapRenderer, Font font, ItemRenderer itemRenderer,
                                                NoticeBoardBlockTile tile, PoseStack poseStack, MultiBufferSource buffer,
                                                int frontLight, int overlay, ItemStack stack, Direction dir,
                                                LOD lod) {

        if (tile.isGlowing()) frontLight = LightTexture.FULL_BRIGHT;

        //render map
        if (stack.getItem() instanceof ComplexItem) {
            MapItemSavedData mapData = MapItem.getSavedData(stack, tile.getLevel());
            if (mapData != null) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 0.008);
                poseStack.scale(0.0078125F, -0.0078125F, -0.0078125F);
                poseStack.translate(-64.0D, -64.0D, 0.0D);
                Integer integer = MapItem.getMapId(stack);
                mapRenderer.render(poseStack, buffer, integer, mapData, true, frontLight);
                poseStack.popPose();
            } else {
                //request map data from server
                Player player = Minecraft.getInstance().player;
                NetworkHandler.CHANNEL.sendToServer(new ServerBoundRequestMapDataPacket(tile.getBlockPos(), player.getUUID()));
            }
            return;
        }

        //render book
        String page = tile.getText();
        if (!(page == null || page.equals(""))) {

            if (!lod.isNearMed()) {
                return;
            }

            poseStack.pushPose();
            poseStack.translate(0, 0.5, 0.008);

            if (MiscUtils.FESTIVITY.isAprilsFool()) {
                float d0 = ColorUtils.getShading(dir.step());
                TextUtils.renderBeeMovie(poseStack, buffer, frontLight, font, d0);
                poseStack.popPose();
                return;
            }

            String bookName = tile.getItem(0).getHoverName().getString().toLowerCase(Locale.ROOT);
            if (bookName.equals("credits")) {
                float d0 = ColorUtils.getShading(dir.step());
                TextUtils.renderCredits(poseStack, buffer, frontLight, font, d0);
                poseStack.popPose();
                return;
            }
            var textProperties = tile.getTextHolder()
                    .computeRenderProperties(frontLight, dir.step(), lod::isVeryNear);

            if (tile.needsVisualUpdate()) {
                updateAndCacheLines(font, tile, page, textProperties);
            }
            List<FormattedCharSequence> rendererLines = tile.getCachedLines();

            float scale = tile.getFontScale();
            poseStack.scale(scale, -scale, scale);
            int numberOfLines = rendererLines.size();
            boolean centered = ClientConfigs.Blocks.NOTICE_BOARD_CENTERED_TEXT.get();

            //maybe use texture renderer for this so we can use shading (not just block shade)

            boolean missingno = bookName.equals("missingno");
            for (int lin = 0; lin < numberOfLines; ++lin) {
                FormattedCharSequence str = rendererLines.get(lin);
                //border offsets. always add 0.5 to center properly
                float dx = centered ? (-font.width(str) / 2f) + 0.5f : -(0.5f - PAPER_X_MARGIN) / scale;
                float dy = (((1f / scale) - (8 * numberOfLines)) / 2f) + 0.5f;
                Matrix4f pose = poseStack.last().pose();
                if (missingno) {
                    font.drawInBatch("§ka", dx, dy + 8 * lin, textProperties.textColor(), false, pose,
                            buffer, Font.DisplayMode.NORMAL, 0, frontLight);
                } else {
                    if (textProperties.outline()) {
                        font.drawInBatch8xOutline(str, dx, dy + 8 * lin, textProperties.textColor(), textProperties.darkenedColor(),
                                pose, buffer, textProperties.light());
                    } else {
                        font.drawInBatch(str, dx, dy + 8 * lin, textProperties.darkenedColor(), false,
                                pose, buffer, Font.DisplayMode.NORMAL, 0, textProperties.light());
                    }
                }
            }
            //cant use because aligned to left & missingno thing
            //TextUtil.renderAllLines(rendererLines.toArray(FormattedCharSequence[]::new),8, font,
            //        poseStack, buffer, textProperties);

            poseStack.popPose();
            return;
        }

        //render item

        Material pattern = tile.getCachedPattern();
        if (pattern != null) {
            VertexConsumer builder = pattern.buffer(buffer, RenderType::entityNoOutline);
            int i =  tile.getDyeColor().getTextColor();
            float scale = 0.5f;//so its more similar to text. idk why its needed
            int b = (int) (scale* (FastColor.ARGB32.blue(i)));
            int g = (int) (scale*(FastColor.ARGB32.green(i)));
            int r = (int) (scale*(FastColor.ARGB32.red(i)));
            int lu = frontLight & '\uffff';
            int lv = frontLight >> 16 & '\uffff';
            poseStack.translate(0,0,0.008f);
            VertexUtil.addQuad(builder, poseStack, -0.4375F, -0.4375F,  0.4375F, 0.4375F,
                    0.15625f, 0.0625f, 0.5f + 0.09375f, 1 - 0.0625f, r, g, b, 255, lu, lv);

        } else if (!tile.isNormalItem()) {
            BakedModel model = itemRenderer.getModel(stack, tile.getLevel(), null, 0);

            poseStack.translate(0, 0, 0.015625 + 0.00005);
            poseStack.scale(-0.5f, 0.5f, -0.5f);
            itemRenderer.render(stack, ItemDisplayContext.FIXED, true, poseStack, buffer, frontLight, overlay, model);
        }

    }

    private static void updateAndCacheLines(Font font, NoticeBoardBlockTile tile, String page, TextUtil.RenderProperties textProperties) {
        float paperWidth = 1 - (2 * PAPER_X_MARGIN);
        float paperHeight = 1 - (2 * PAPER_Y_MARGIN);
        var text = TextUtil.parseText(page);
        if(text instanceof MutableComponent mc){
            text = mc.setStyle(textProperties.style());
        }else{
            text = Component.literal(page).setStyle(textProperties.style());
        }
        var p = TextUtil.fitLinesToBox(font,
                text, paperWidth, paperHeight);
        tile.setFontScale(p.getSecond());
        tile.setCachedPageLines(p.getFirst());
    }

}