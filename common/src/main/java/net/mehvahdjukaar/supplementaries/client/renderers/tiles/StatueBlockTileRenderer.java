package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.StatueEntityModel;
import net.mehvahdjukaar.supplementaries.common.block.tiles.StatueBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;


public class StatueBlockTileRenderer implements BlockEntityRenderer<StatueBlockTile> {
    private final ItemRenderer itemRenderer;
    private final StatueEntityModel model;
    private final BlockRenderDispatcher blockRenderer;
    private final EntityRenderDispatcher entityRenderer;
    private final SkinManager skinManager;

    public StatueBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        model = new StatueEntityModel(context);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
        entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
        skinManager = Minecraft.getInstance().getSkinManager();
    }

    //same as entity logic
    protected boolean canRenderName(StatueBlockTile tile) {
        if (Minecraft.renderNames()) {
            HitResult hit = Minecraft.getInstance().hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = tile.getBlockPos();
                BlockPos hitPos = ((BlockHitResult) hit).getBlockPos();
                if (pos.equals(hitPos)) {
                    double d0 = entityRenderer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    return d0 < 16 * 16;
                }
            }
        }
        return false;
    }

    @Override
    public void render(StatueBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        ResolvableProfile gameProfile = tile.getPlayerSkin();

        if (gameProfile != null && this.canRenderName(tile)) {
            var name = gameProfile.gameProfile().getName();
            if (name != null) {
                PedestalBlockTileRenderer.renderName(Component.literal(name), 0.875f, poseStack, bufferIn, combinedLightIn);
            }
        }
        ResourceLocation skinTexture;
        ResourceLocation capeTexture;
        boolean slim;
        if (gameProfile == null) {
            skinTexture = ModTextures.STATUE;
            slim = false;
            capeTexture = null;
        } else {
            PlayerSkin playerSkin = skinManager.getInsecureSkin(gameProfile.gameProfile());
            skinTexture = playerSkin.texture();
            slim = playerSkin.model() == PlayerSkin.Model.SLIM;
            capeTexture = playerSkin.capeTexture();
        }

        Direction dir = tile.getDirection();
        poseStack.mulPose(RotHlpr.rot(dir));
        poseStack.scale(-1, -1, 1);

        poseStack.translate(0, -0.25, 0);

        RenderType renderType = RenderType.entityTranslucent(skinTexture);

        StatueBlockTile.StatuePose pose = tile.getPose();
        ItemStack stack = tile.getDisplayedItem();

        if (MiscUtils.FESTIVITY.isHalloween()) {
            this.model.head.visible = false;
            this.model.hat.visible = false;
            if (pose == StatueBlockTile.StatuePose.STANDING) {
                pose = StatueBlockTile.StatuePose.HOLDING;
                stack = Items.JACK_O_LANTERN.getDefaultInstance();
            } else {
                poseStack.pushPose();
                poseStack.scale(-0.625f, -0.625f, 0.625f);

                poseStack.translate(0, 0.1875, 0);
                itemRenderer.renderStatic(Items.CARVED_PUMPKIN.getDefaultInstance(), ItemDisplayContext.FIXED,
                        combinedLightIn, combinedOverlayIn, poseStack, bufferIn, tile.getLevel(), 0);
                poseStack.popPose();
            }
        } else if (MiscUtils.FESTIVITY.isChristmas() || MiscUtils.FESTIVITY.isBirthday() ||
                MiscUtils.FESTIVITY.isAprilsFool()) {
            poseStack.pushPose();
            poseStack.scale(-0.625f, -0.625f, 0.625f);

            poseStack.translate(0, 0.1875, 0);
            itemRenderer.renderStatic(ModRegistry.CONFETTI_POPPER.get().getDefaultInstance(),
                    ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn,
                    poseStack, bufferIn, tile.getLevel(), 0);
            poseStack.popPose();
        } else {
            this.model.head.visible = true;
            this.model.hat.visible = true;
        }


        poseStack.pushPose();
        poseStack.scale(0.5f, +0.499f, 0.5f);
        VertexConsumer buffer = bufferIn.getBuffer(renderType);

        this.model.setupAnim(tile.getLevel().getGameTime(), partialTicks, dir, pose, tile.isWaving(), slim);
        this.model.renderToBuffer(poseStack, buffer, combinedLightIn, OverlayTexture.NO_OVERLAY, -1);

        if (capeTexture != null) {
            RenderType capeType = RenderType.entityTranslucent(capeTexture);
            VertexConsumer capeBuffer = bufferIn.getBuffer(capeType);
            this.model.renderCloak(poseStack, capeBuffer, combinedLightIn, combinedOverlayIn,
                    tile, partialTicks);
        }

        poseStack.popPose();


        switch (pose) {
            case STANDING:
                break;
            case CANDLE:
                poseStack.scale(1f, -1f, -1f);
                poseStack.translate(-0.5, -0.6875, -0.3125);
                blockRenderer.renderSingleBlock(tile.hasCandle(), poseStack, bufferIn, combinedLightIn, combinedOverlayIn);
                break;
            default:
                //holding
                poseStack.scale(-0.5f, -0.5f, 0.5f);
                BakedModel itemModel = itemRenderer.getModel(stack, tile.getLevel(), null, 0);

                if (pose == StatueBlockTile.StatuePose.SWORD) {
                    poseStack.translate(-0.35, -1.0625, 0.0);
                    poseStack.mulPose(RotHlpr.Z135);
                } else if (pose == StatueBlockTile.StatuePose.TOOL) {
                    poseStack.translate(-0.4, -1.25, 0.0);
                    poseStack.mulPose(RotHlpr.Z135);
                }

                poseStack.translate(0, -0.5, -0.5);
                if (pose.isGlobe()) {
                    if (GlobeBlockTileRenderer.INSTANCE != null) {

                        boolean sepia = pose == StatueBlockTile.StatuePose.SEPIA_GLOBE;
                        Pair<GlobeManager.Model, ResourceLocation> pair =
                                stack.has(DataComponents.CUSTOM_NAME) ?
                                        GlobeManager.getModelAndTexture(stack.getHoverName().getString()) :
                                        Pair.of(GlobeManager.Model.GLOBE, null);

                        GlobeBlockTileRenderer.INSTANCE.renderGlobe(pair, poseStack, bufferIn,
                                combinedLightIn, combinedOverlayIn, sepia, tile.getLevel());
                    }
                } else {
                    this.itemRenderer.render(stack, ItemDisplayContext.FIXED, true, poseStack, bufferIn, combinedLightIn,
                            combinedOverlayIn, itemModel);

                }

        }


        poseStack.popPose();
    }

}