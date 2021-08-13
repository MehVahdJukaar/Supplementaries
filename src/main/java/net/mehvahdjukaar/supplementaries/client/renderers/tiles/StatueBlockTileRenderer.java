package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.StatueBlockTile;
import net.mehvahdjukaar.supplementaries.client.models.StatueEntityModel;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
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
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.Map;


public class StatueBlockTileRenderer extends TileEntityRenderer<StatueBlockTile> {
    protected final ItemRenderer itemRenderer;
    private final StatueEntityModel model;

    public StatueBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        model = new StatueEntityModel(0);
    }
    private boolean slim = false;

    private ResourceLocation getSkin(GameProfile gameProfile) {
        if (!gameProfile.isComplete()) {
            return new ResourceLocation("minecraft:textures/entity/steve.png");
        }
        else {
            Minecraft minecraft = Minecraft.getInstance();
            SkinManager skinManager = minecraft.getSkinManager();

            Map<Type, MinecraftProfileTexture> loadSkinFromCache = skinManager.getInsecureSkinInformation(gameProfile); // returned map may or may not be typed
            if (loadSkinFromCache.containsKey(Type.SKIN)) {
                MinecraftProfileTexture texture = loadSkinFromCache.get(Type.SKIN);
                String s = texture.getMetadata("model");
                this.slim = s!=null && !s.equals("default");

                return skinManager.registerTexture(texture, Type.SKIN);
            }
            else {
                return DefaultPlayerSkin.getDefaultSkin(gameProfile.getId());
            }
        }
    }

    private boolean isSkinSlim(GameProfile gameProfile){

        return gameProfile != null && gameProfile.getId() != null && (gameProfile.getId().hashCode() & 1) == 1;
    }

    @Override
    public void render(StatueBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        GameProfile playerInfo = tile.playerProfile;
        ResourceLocation resourceLocation = tile.playerProfile ==null ? Textures.STATUE : getSkin(playerInfo);
        matrixStackIn.translate(0.5,0.5,0.5);
        Direction dir = tile.getDirection();
        matrixStackIn.mulPose(Const.rot(dir));
        matrixStackIn.mulPose(Const.X90);

        matrixStackIn.translate(0,-0.25,0);

        //
        RenderType renderType = RenderType.entityCutout(resourceLocation);


        if (renderType != null) {
            matrixStackIn.pushPose();
            matrixStackIn.scale(0.5f,+0.499f,0.5f);
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(renderType);

            this.model.setupAnim(tile.getLevel().getGameTime(),partialTicks, dir, tile.pose, tile.isWaving, slim);
            this.model.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,   1.0F);
            matrixStackIn.popPose();
        }
        slim = false;


        ItemStack stack = tile.getDisplayedItem();

        if(tile.pose == StatueBlockTile.StatuePose.CANDLE){
            matrixStackIn.scale(1f,-1f,-1f);
            //matrixStackIn.translate(0,-0.75,0.5);
            //blockRenderer.renderBlock(tile.candle, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        }
        else{

            matrixStackIn.scale(-0.5f,-0.5f,0.5f);
            IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);

            if(tile.pose == StatueBlockTile.StatuePose.SWORD) {
                matrixStackIn.translate(-0.35,-1.0625,0.0);
                matrixStackIn.mulPose(Const.Z135);
            }
            else if(tile.pose == StatueBlockTile.StatuePose.TOOL){
                matrixStackIn.translate(-0.4,-1.25,0.0);
                matrixStackIn.mulPose(Const.Z135);
            }


            matrixStackIn.translate(0,-0.5,-0.5);
            itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                    combinedOverlayIn, ibakedmodel);
        }



        matrixStackIn.popPose();
    }




}