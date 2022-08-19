package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.block_models.StatueEntityModel;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.StatueBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;

import java.util.Map;
import java.util.function.Consumer;


public class StatueBlockTileRenderer implements BlockEntityRenderer<StatueBlockTile> {
    protected final ItemRenderer itemRenderer;
    private final StatueEntityModel model;
    private final BlockRenderDispatcher blockRenderer;
    private final EntityRenderDispatcher entityRenderer;

    public StatueBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        model = new StatueEntityModel(context);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
        entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    protected boolean canRenderName(StatueBlockTile tile) {
        if (Minecraft.renderNames() && tile.owner != null) {
            HitResult hit = Minecraft.getInstance().hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = tile.getBlockPos();
                BlockPos hitPos = new BlockPos(hit.getLocation());
                if (pos.equals(hitPos)) {
                    double d0 = entityRenderer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    return d0 < 16 * 16;
                }
            }
        }
        return false;
    }

    @Override
    public int getViewDistance() {
        return 60;
    }

    private boolean slim = false;

    public static ResourceLocation getPlayerSkin(GameProfile gameProfile) {
        return getPlayerSkinAndSlim(gameProfile, s -> {
        });
    }

    public static ResourceLocation getPlayerSkinAndSlim(GameProfile gameProfile, Consumer<Boolean> slimSkinSetter) {
        if (!gameProfile.isComplete()) {
            return new ResourceLocation("minecraft:textures/entity/steve.png");
        } else {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();

            Map<Type, MinecraftProfileTexture> skinCache = skinManager.getInsecureSkinInformation(gameProfile); // returned map may or may not be typed
            if (skinCache.containsKey(Type.SKIN)) {
                MinecraftProfileTexture texture = skinCache.get(Type.SKIN);
                String s = texture.getMetadata("model");
                boolean slim = s != null && !s.equals("default");
                slimSkinSetter.accept(slim);

                return skinManager.registerTexture(texture, Type.SKIN);
            } else {
                slimSkinSetter.accept(false);
                return DefaultPlayerSkin.getDefaultSkin(gameProfile.getId());
            }
        }
    }

    private boolean isSkinSlim(GameProfile gameProfile) {
        return gameProfile != null && gameProfile.getId() != null && (gameProfile.getId().hashCode() & 1) == 1;
    }

    @Override
    public void render(StatueBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        GameProfile playerInfo = tile.owner;

        if (this.canRenderName(tile)) {
            var name = tile.owner.getName();
            if (name != null) {
                PedestalBlockTileRenderer.renderName(Component.literal(name), 0.875f, matrixStackIn, bufferIn, combinedLightIn);
            }
        }

        ResourceLocation resourceLocation = tile.owner == null ? ModTextures.STATUE : getPlayerSkinAndSlim(playerInfo, s -> this.slim = s);

        Direction dir = tile.getDirection();
        matrixStackIn.mulPose(RotHlpr.rot(dir));
        matrixStackIn.mulPose(RotHlpr.X90);

        matrixStackIn.translate(0, -0.25, 0);

        RenderType renderType = RenderType.entityTranslucent(resourceLocation);

        StatueBlockTile.StatuePose pose = tile.pose;
        ItemStack stack = tile.getDisplayedItem();

        if (CommonUtil.FESTIVITY.isHalloween()) {
            this.model.head.visible = false;
            this.model.hat.visible = false;
            if (pose == StatueBlockTile.StatuePose.STANDING) {
                pose = StatueBlockTile.StatuePose.HOLDING;
                stack = Items.JACK_O_LANTERN.getDefaultInstance();
            } else {
                matrixStackIn.pushPose();
                matrixStackIn.scale(-0.625f, -0.625f, 0.625f);

                matrixStackIn.translate(0, 0.1875, 0);
                itemRenderer.renderStatic(Items.CARVED_PUMPKIN.getDefaultInstance(), ItemTransforms.TransformType.FIXED,
                        combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, 0);
                matrixStackIn.popPose();
            }
        } else {
            this.model.head.visible = true;
            this.model.hat.visible = true;
        }


        matrixStackIn.pushPose();
        matrixStackIn.scale(0.5f, +0.499f, 0.5f);
        VertexConsumer buffer = bufferIn.getBuffer(renderType);

        this.model.setupAnim(tile.getLevel().getGameTime(), partialTicks, dir, pose, tile.isWaving, slim);
        this.model.renderToBuffer(matrixStackIn, buffer, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.popPose();
        this.slim = false;


        switch (pose) {
            case STANDING:
                break;
            case CANDLE:
                matrixStackIn.scale(1f, -1f, -1f);
                matrixStackIn.translate(-0.5, -0.6875, -0.3125);
                blockRenderer.renderSingleBlock(tile.candle, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
                break;
            default:
                //holding
                matrixStackIn.scale(-0.5f, -0.5f, 0.5f);
                BakedModel model = itemRenderer.getModel(stack, tile.getLevel(), null, 0);

                if (pose == StatueBlockTile.StatuePose.SWORD) {
                    matrixStackIn.translate(-0.35, -1.0625, 0.0);
                    matrixStackIn.mulPose(RotHlpr.Z135);
                } else if (pose == StatueBlockTile.StatuePose.TOOL) {
                    matrixStackIn.translate(-0.4, -1.25, 0.0);
                    matrixStackIn.mulPose(RotHlpr.Z135);
                }

                matrixStackIn.translate(0, -0.5, -0.5);
                if (pose.isGlobe()) {
                    if (GlobeBlockTileRenderer.INSTANCE != null) {

                        boolean sepia = pose == StatueBlockTile.StatuePose.SEPIA_GLOBE;
                        Pair<GlobeBlockTile.GlobeModel, ResourceLocation> pair =
                                stack.hasCustomHoverName() ?
                                        GlobeBlockTile.GlobeType.getGlobeTexture(stack.getHoverName().getString()) :
                                        Pair.of(GlobeBlockTile.GlobeModel.GLOBE, null);

                        GlobeBlockTileRenderer.INSTANCE.renderGlobe(pair, matrixStackIn, bufferIn,
                                combinedLightIn, combinedOverlayIn, sepia, tile.getLevel());
                    }
                } else {
                    this.itemRenderer.render(stack, ItemTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                            combinedOverlayIn, model);

                }

        }


        matrixStackIn.popPose();
    }

}