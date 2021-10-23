package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;


public class GlobeBlockTileRenderer implements BlockEntityRenderer<GlobeBlockTile> {

    private final ModelPart globe = new ModelPart(32, 16, 0, 0);
    private final ModelPart flat = new ModelPart(32, 32, 0, 0);
    private final ModelPart sheared = new ModelPart(32, 32, 0, 0);
    private final ModelPart snow = new ModelPart(32, 32, 0, 0);


    public GlobeBlockTileRenderer(BlockEntityRendererProvider.Context context) {

        globe.addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        globe.setPos(0.0F, 24.0F, 0.0F);

        flat.setPos(0.0F, 24.0F, 0.0F);
        flat.texOffs(0, 0).addBox(-4.0F, -28.0F, -4.0F, 8.0F, 4.0F, 8.0F, 0.0F, false);
        flat.texOffs(0, 13).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 2.0F, 8.0F, 0.0F, false);
        flat.texOffs(4, 23).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
        flat.texOffs(8, 24).addBox(-2.0F, -21.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);

        sheared.setPos(0.0F, 24.0F, 0.0F);
        sheared.texOffs(0, 0).addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 4.0F, 0.0F, false);
        sheared.texOffs(0, 12).addBox(0.0F, -28.0F, 0.0F, 4.0F, 8.0F, 4.0F, 0.0F, false);


        snow.setPos(0.0F, 24.0F, 0.0F);
        snow.texOffs(0, 0).addBox(-4.0F, -28.0F, -4.0F, 8.0F, 5.0F, 8.0F, 0.0F, false);
        snow.texOffs(0, 14).addBox(-4.0F, -23.0F, -4.0F, 8.0F, 1.0F, 8.0F, 0.0F, false);
        snow.texOffs(4, 16).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
        snow.texOffs(0, 17).addBox(-2.0F, -24.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);
        snow.texOffs(0, 28).addBox(-1.0F, -25.975F, -1.0F, 2.0F, 2.0F, 2.0F, -0.05F, false);
        snow.texOffs(12, 20).addBox(-1.0F, -21.0F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        ModelPart roof = new ModelPart(32, 32, 0, 0);
        roof.setPos(0.0F, -25.9F, 0.0F);
        snow.addChild(roof);

        ModelPart flat_r1 = new ModelPart(32, 32, 0, 0);
        flat_r1.setPos(0.0F, 0.0F, 0.0F);
        roof.addChild(flat_r1);
        flat_r1.zRot = 0.7854F;
        flat_r1.texOffs(11, 27).addBox(0.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        ModelPart flat_r2 = new ModelPart(32, 32, 0, 0);
        flat_r2.setPos(0.0F, 0.0F, 0.0F);
        roof.addChild(flat_r2);
        flat_r2.zRot = -0.7854F;
        flat_r2.texOffs(0, 27).addBox(-2.0F, -1.0F, -1.0F, 3.0F, 1.0F, 2.0F, 0.0F, false);
    }

    @Override
    public void render(GlobeBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5,0.5,0.5);
        matrixStackIn.mulPose(Const.rot(tile.getDirection()));
        matrixStackIn.mulPose(Const.XN90);
        matrixStackIn.translate(0,+0.0625,0);
        matrixStackIn.mulPose(Const.XN22);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, tile.prevYaw+tile.face, tile.yaw+tile.face)));
        matrixStackIn.mulPose(Const.X180);

        VertexConsumer builder;

        ResourceLocation texture = ClientConfigs.cached.GLOBE_RANDOM ? tile.texture : GlobeBlockTile.GlobeType.EARTH.texture;

        ModelPart selected;

        if(tile.sheared){
            selected = sheared;
            texture = Textures.GLOBE_SHEARED_TEXTURE;
        }
        else if(tile.isFlat){
            selected = flat;
            texture = Textures.GLOBE_FLAT_TEXTURE;
        }
        else if(tile.isSnow){
            selected = snow;
        }
        else{
            selected = globe;
        }

        if(texture==null){
            builder = bufferIn.getBuffer(GlobeTextureManager.INSTANCE.getRenderType(tile.getLevel()));
        }
        else{
            builder = bufferIn.getBuffer(RenderType.entityCutout(texture));
        }


        selected.render(matrixStackIn, builder, combinedLightIn,combinedOverlayIn,1,1,1,1);

        matrixStackIn.popPose();
    }

}