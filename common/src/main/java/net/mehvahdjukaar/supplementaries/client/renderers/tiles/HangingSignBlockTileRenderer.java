package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.renderUtils.LOD;
import net.mehvahdjukaar.moonlight.api.client.renderUtils.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.TextUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HangingSignBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestMapDataPacket;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class HangingSignBlockTileRenderer implements BlockEntityRenderer<HangingSignBlockTile> {
    public static final int LINE_MAX_WIDTH = 75;
    public static final int LINE_SEPARATION = 10;

    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;
    private final MapRenderer mapRenderer;
    private final Camera camera;
    private final Font font;

    public HangingSignBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft minecraft = Minecraft.getInstance();
        blockRenderer = context.getBlockRenderDispatcher();
        itemRenderer = minecraft.getItemRenderer();
        mapRenderer = minecraft.gameRenderer.getMapRenderer();
        camera = minecraft.gameRenderer.getMainCamera();
        font = context.getFont();
    }

    @Override
    public void render(HangingSignBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        poseStack.pushPose();
        BlockState state = tile.getBlockState();
        BlockProperties.SignAttachment attachment = state.getValue(BlockProperties.SIGN_ATTACHMENT);
        //rotate towards direction
        double dy = attachment == BlockProperties.SignAttachment.CEILING ? 1 : 0.875;

        poseStack.translate(0.5, dy, 0.5);
        if(state.getValue(HangingSignBlock.AXIS) == Direction.Axis.X) poseStack.mulPose(RotHlpr.Y90);

        //animation

        if(tile.shouldRenderFancy()) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(tile.getSwingAngle(partialTicks)));

            poseStack.translate(-0.5, -0.875, -0.5);
            //render block
            RendererUtil.renderBlockModel(ClientRegistry.HANGING_SIGNS_BLOCK_MODELS.get(tile.woodType), poseStack, bufferIn, blockRenderer, combinedLightIn, combinedOverlayIn, true);
        }else{
            poseStack.translate(-0.5, -0.875, -0.5);
        }
        LOD lod = new LOD(camera, tile.getBlockPos());

        tile.setFancyRenderer(lod.isNear());

        if (lod.isMedium()) {
            poseStack.translate(0.5, 0.5 - 0.1875, 0.5);
            poseStack.mulPose(RotHlpr.YN90);
            // render item
            if (!tile.isEmpty()) {
                ItemStack stack = tile.getStackInSlot(0);
                Item item = stack.getItem();

                //render map
                if (item instanceof ComplexItem) {
                    MapItemSavedData mapData = MapItem.getSavedData(stack, tile.getLevel());
                    if (mapData != null) {
                        for (int v = 0; v < 2; v++) {
                            poseStack.pushPose();
                            poseStack.translate(0, 0, 0.0625 + 0.005);
                            poseStack.scale(0.0068359375F, -0.0068359375F, -0.0068359375F);
                            poseStack.translate(-64.0D, -64.0D, 0.0D);
                            Integer mapId = MapItem.getMapId(stack);
                            mapRenderer.render(poseStack, bufferIn, mapId, mapData, true, combinedLightIn);
                            poseStack.popPose();

                            poseStack.mulPose(RotHlpr.Y180);
                        }
                    } else {
                        //request map data from server
                        Player player = Minecraft.getInstance().player;
                        NetworkHandler.INSTANCE.sendToServer(new ServerBoundRequestMapDataPacket(tile.getBlockPos(), player.getUUID()));
                    }
                } else if (item instanceof BannerPatternItem) {

                    //TODO: cache or not like notice board
                    Material renderMaterial = ClientRegistry.FLAG_MATERIALS.get(((BannerPatternItem) item).getBannerPattern());

                    VertexConsumer builder = renderMaterial.buffer(bufferIn, RenderType::entityNoOutline);

                    float[] color = tile.textHolder.getColor().getTextureDiffuseColors();
                    float b = color[2];
                    float g = color[1];
                    float r = color[0];
                    int lu = combinedLightIn & '\uffff';
                    int lv = combinedLightIn >> 16 & '\uffff';
                    for (int v = 0; v < 2; v++) {
                        RendererUtil.addQuadSide(builder, poseStack, -0.4375F, -0.4375F, 0.0725f, 0.4375F, 0.4375F, 0.07f,
                                0.15625f, 0.0625f, 0.5f + 0.09375f, 1 - 0.0625f, r, g, b, 1, lu, lv, 0, 0, 1, renderMaterial.sprite());

                        poseStack.mulPose(RotHlpr.Y180);
                    }
                }
                //render item
                else {
                    BakedModel model = itemRenderer.getModel(stack, tile.getLevel(), null, 0);
                    for (int v = 0; v < 2; v++) {
                        poseStack.pushPose();
                        poseStack.scale(0.75f, 0.75f, 0.75f);
                        poseStack.translate(0, 0, -0.1);
                        //poseStack.mulPose(Const.Y180);
                        itemRenderer.render(stack, ItemTransforms.TransformType.FIXED, true, poseStack, bufferIn, combinedLightIn,
                                combinedOverlayIn, model);
                        poseStack.popPose();

                        poseStack.mulPose(RotHlpr.Y180);
                        poseStack.scale(0.9995f, 0.9995f, 0.9995f);
                    }
                }
            }

            // render text
            else if (lod.isNearMed()) {
                poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);

                TextUtil.RenderTextProperties textProperties = new TextUtil.RenderTextProperties(tile.getTextHolder(), combinedLightIn, lod::isVeryNear);

                for (int v = 0; v < 2; v++) {
                    poseStack.pushPose();
                    poseStack.translate(0, -34, (0.0625 + 0.005) / 0.010416667F);

                    TextUtil.renderAllLines(tile.textHolder, LINE_SEPARATION, this.font, LINE_MAX_WIDTH,
                            poseStack, bufferIn, textProperties);

                    poseStack.popPose();
                    poseStack.mulPose(RotHlpr.Y180);
                }
            }
        }
        poseStack.popPose();
    }
}