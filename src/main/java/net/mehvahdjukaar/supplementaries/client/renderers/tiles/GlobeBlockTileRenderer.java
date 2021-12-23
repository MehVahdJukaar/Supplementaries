package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;


public class GlobeBlockTileRenderer implements BlockEntityRenderer<GlobeBlockTile> {

    private final ModelPart globe;
    private final ModelPart flat;
    private final ModelPart sheared;
    private final ModelPart snow;

    public static LayerDefinition createBaseMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("globe", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0, 24, 0));

        return LayerDefinition.create(mesh, 32, 16);
    }

    public static LayerDefinition createSpecialMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("flat", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 4.0F, 8.0F)
                        .texOffs(0, 13)
                        .addBox(-4.0F, -24.0F, -4.0F, 8.0F, 2.0F, 8.0F)
                        .texOffs(4, 23)
                        .addBox(-3.0F, -22.0F, -3.0F, 6.0F, 1.0F, 6.0F)
                        .texOffs(8, 24)
                        .addBox(-2.0F, -21.0F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(0, 24, 0));

        root.addOrReplaceChild("sheared", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 4.0F)
                        .texOffs(0, 12)
                        .addBox(0.0F, -28.0F, 0.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(0, 24, 0));

        PartDefinition snow = root.addOrReplaceChild("snow", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 5.0F, 8.0F)
                        .texOffs(0, 14)
                        .addBox(-4.0F, -23.0F, -4.0F, 8.0F, 1.0F, 8.0F)
                        .texOffs(4, 16)
                        .addBox(-3.0F, -22.0F, -3.0F, 6.0F, 1.0F, 6.0F)
                        .texOffs(0, 17)
                        .addBox(-2.0F, -24.0F, -2.0F, 4.0F, 1.0F, 4.0F)
                        .texOffs(0, 28)
                        .addBox(-1.0F, -25.975F, -1.0F, 2.0F, 2.0F, 2.0F)
                        .texOffs(12, 20)
                        .addBox(-1.0F, -21.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0, 24, 0));
        snow.addOrReplaceChild("roof_l", CubeListBuilder.create()
                        .texOffs(11, 27)
                        .addBox(0.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0, -25.9F, 0, 0, 0, 0.7854F));
        snow.addOrReplaceChild("roof_r", CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(-2.0F, -1.0F, -1.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0, -25.9F, 0, 0, 0,  -0.7854F));


        return LayerDefinition.create(mesh, 32, 32);
    }

    public GlobeBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.GLOBE_BASE_MODEL);
        this.globe = model.getChild("globe");
        ModelPart special = context.bakeLayer(ClientRegistry.GLOBE_SPECIAL_MODEL);
        this.flat = special.getChild("flat");
        this.snow = special.getChild("snow");
        this.sheared = special.getChild("sheared");
    }

    @Override
    public void render(GlobeBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(Const.rot(tile.getDirection()));
        matrixStackIn.mulPose(Const.XN90);
        matrixStackIn.translate(0, +0.0625, 0);
        matrixStackIn.mulPose(Const.XN22);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, tile.prevYaw + tile.face, tile.yaw + tile.face)));
        matrixStackIn.mulPose(Const.X180);

        VertexConsumer builder;

        ResourceLocation texture = ClientConfigs.cached.GLOBE_RANDOM ? tile.texture : GlobeBlockTile.GlobeType.EARTH.texture;

        ModelPart selected;

        if (tile.sheared) {
            selected = sheared;
            texture = Textures.GLOBE_SHEARED_TEXTURE;
        } else if (tile.isFlat) {
            selected = flat;
            texture = Textures.GLOBE_FLAT_TEXTURE;
        } else if (tile.isSnow) {
            selected = snow;
        } else {
            selected = globe;
        }

        if (texture == null) {
            builder = bufferIn.getBuffer(GlobeTextureManager.INSTANCE.getRenderType(tile.getLevel(), tile.isSepia()));
        } else {
            builder = bufferIn.getBuffer(RenderType.entityCutout(texture));
        }


        selected.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);

        matrixStackIn.popPose();
    }

}