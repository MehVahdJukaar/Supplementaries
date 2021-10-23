package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.block.tiles.StatueBlockTile;
import net.mehvahdjukaar.supplementaries.client.models.StatueEntityModel;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;


public class StatueBlockTileRenderer implements BlockEntityRenderer<StatueBlockTile> {
    protected final ItemRenderer itemRenderer;
    private final StatueEntityModel model;

    public StatueBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        model = new StatueEntityModel(0);
    }

    @Override
    public int getViewDistance() {
        return 60;
    }

    private boolean slim = false;

    private ResourceLocation getSkin(GameProfile gameProfile) {
        if (!gameProfile.isComplete()) {
            return new ResourceLocation("minecraft:textures/entity/steve.png");
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            SkinManager skinManager = minecraft.getSkinManager();

            Map<Type, MinecraftProfileTexture> loadSkinFromCache = skinManager.getInsecureSkinInformation(gameProfile); // returned map may or may not be typed
            if (loadSkinFromCache.containsKey(Type.SKIN)) {
                MinecraftProfileTexture texture = loadSkinFromCache.get(Type.SKIN);
                String s = texture.getMetadata("model");
                this.slim = s != null && !s.equals("default");

                return skinManager.registerTexture(texture, Type.SKIN);
            } else {
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
        GameProfile playerInfo = tile.playerProfile;
        ResourceLocation resourceLocation = tile.playerProfile == null ? Textures.STATUE : getSkin(playerInfo);
        matrixStackIn.translate(0.5, 0.5, 0.5);
        Direction dir = tile.getDirection();
        matrixStackIn.mulPose(Const.rot(dir));
        matrixStackIn.mulPose(Const.X90);

        matrixStackIn.translate(0, -0.25, 0);

        //
        RenderType renderType = RenderType.entityCutout(resourceLocation);

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
                        combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
                matrixStackIn.popPose();
            }
        } else {
            this.model.head.visible = true;
            this.model.hat.visible = true;
        }


        if (renderType != null) {
            matrixStackIn.pushPose();
            matrixStackIn.scale(0.5f, +0.499f, 0.5f);
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(renderType);

            this.model.setupAnim(tile.getLevel().getGameTime(), partialTicks, dir, pose, tile.isWaving, slim);
            this.model.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStackIn.popPose();
        }
        this.slim = false;


        switch (pose) {
            case STANDING:
                break;
            case CANDLE:
                matrixStackIn.scale(1f, -1f, -1f);
                //matrixStackIn.translate(0,-0.75,0.5);
                //blockRenderer.renderBlock(tile.candle, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
                break;
            default:
                matrixStackIn.scale(-0.5f, -0.5f, 0.5f);
                BakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);

                if (pose == StatueBlockTile.StatuePose.SWORD) {
                    matrixStackIn.translate(-0.35, -1.0625, 0.0);
                    matrixStackIn.mulPose(Const.Z135);
                } else if (pose == StatueBlockTile.StatuePose.TOOL) {
                    matrixStackIn.translate(-0.4, -1.25, 0.0);
                    matrixStackIn.mulPose(Const.Z135);
                }

                matrixStackIn.translate(0, -0.5, -0.5);
                itemRenderer.render(stack, ItemTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                        combinedOverlayIn, ibakedmodel);

        }


        matrixStackIn.popPose();
    }


}