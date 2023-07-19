package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.StatueBlockTile;
import net.mehvahdjukaar.supplementaries.client.models.StatueEntityModel;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.function.Consumer;


public class StatueBlockTileRenderer extends TileEntityRenderer<StatueBlockTile> {
    protected final ItemRenderer itemRenderer;
    private final StatueEntityModel model;

    public StatueBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        model = new StatueEntityModel(0);
    }

    private boolean slim = false;

    public static ResourceLocation getPlayerSkin(GameProfile gameProfile) {
        return getPlayerSkinAndSlim(gameProfile, s->{});
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
    public void render(StatueBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        GameProfile playerInfo = tile.owner;
        ResourceLocation resourceLocation = tile.owner == null ? Textures.STATUE : getSkin(playerInfo);
        matrixStackIn.translate(0.5, 0.5, 0.5);
        Direction dir = tile.getDirection();
        matrixStackIn.mulPose(Const.rot(dir));
        matrixStackIn.mulPose(Const.X90);

        matrixStackIn.translate(0, -0.25, 0);

        //
        RenderType renderType = RenderType.entityCutout(resourceLocation);

        StatueBlockTile.StatuePose pose = tile.getPose();
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
                itemRenderer.renderStatic(Items.CARVED_PUMPKIN.getDefaultInstance(), ItemCameraTransforms.TransformType.FIXED,
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
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(renderType);

            this.model.setupAnim(tile.getLevel().getGameTime(), partialTicks, dir, pose, tile.isWaving(), slim);
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
                IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);

                if (pose == StatueBlockTile.StatuePose.SWORD) {
                    matrixStackIn.translate(-0.35, -1.0625, 0.0);
                    matrixStackIn.mulPose(Const.Z135);
                } else if (pose == StatueBlockTile.StatuePose.TOOL) {
                    matrixStackIn.translate(-0.4, -1.25, 0.0);
                    matrixStackIn.mulPose(Const.Z135);
                }

                matrixStackIn.translate(0, -0.5, -0.5);
                itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                        combinedOverlayIn, ibakedmodel);

        }


        matrixStackIn.popPose();
    }


}