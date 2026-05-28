package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;


public class WindVaneBlockTileRenderer implements BlockEntityRenderer<WindVaneBlockTile> {

    private final ModelPart model;
    private final BoatModel boatModel;

    public WindVaneBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ClientRegistry.WIND_VANE_MODEL);
        this.boatModel = new BoatModel(context.bakeLayer(ModelLayers.createBoatModelName(Boat.Type.OAK)));
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("chicken",
                CubeListBuilder.create().texOffs(0, -11)
                        .addBox(0.0F, -8.0F, -5.5F, 0.0F, 11.0F, 11.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void render(WindVaneBlockTile tile, float partialTicks, PoseStack ps, MultiBufferSource buffers,
                       int light, int overlay) {
        // Flush any pending batch so nothing drawn before us interferes.
        if (buffers instanceof MultiBufferSource.BufferSource bs) bs.endBatch();

        // ── Depth-prepass plane at block centre (world Y = 0.5) ─────────────
        // Computed in the vane's local space (translate + flip) so Y=0 == block centre.
        ps.pushPose();
        ps.translate(0.5, 0.5, 0.5);
        ps.scale(1, -1, -1);
        drawDepthPlane(ps.last().pose());
        ps.popPose();

        // ── Objects to mask — both queued AFTER the plane is in the depth buffer ─

        // Stone block (0→1 in block space): lower half should vanish when viewed from above.
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.STONE.defaultBlockState(), ps, buffers, light, overlay);

        // Boat model — hollow hull makes the depth-mask cutoff clearly visible.
        ps.pushPose();
        ps.translate(0.5, 0.5, 0.5);
        ps.scale(-1f, -1f, 1f);
        boatModel.renderToBuffer(ps,
                buffers.getBuffer(RenderType.entityCutoutNoCull(
                        ResourceLocation.withDefaultNamespace("textures/entity/boat/oak.png"))),
                light, OverlayTexture.NO_OVERLAY);
        ps.popPose();

        // Wind vane model.
        ps.pushPose();
        ps.translate(0.5, 0.5, 0.5);
        ps.scale(1, -1, -1);
        model.yRot = Mth.DEG_TO_RAD * tile.getYaw(partialTicks);
        model.render(ps, ModMaterials.WIND_VANE_MATERIAL.buffer(buffers, RenderType::entityCutout),
                light, overlay);
        ps.popPose();
    }

    /**
     * Draws a horizontal quad at Y=0 in the supplied matrix space into the depth
     * buffer only — no colour output.  Depth test is disabled so the write is
     * unconditional (solid block geometry already in the buffer would otherwise
     * cause it to fail GL_LEQUAL and write nothing).
     * <p>
     * After this call: any geometry drawn to the same pixels whose depth is greater
     * than the plane's depth (i.e. further from the camera) fails GL_LEQUAL and
     * is invisible — effectively clipping everything "below" the plane from the
     * camera's perspective.
     */
    private static void drawDepthPlane(Matrix4f mat) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder bb = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bb.addVertex(mat, -1f, 0f, -1f);
        bb.addVertex(mat, 1f, 0f, -1f);
        bb.addVertex(mat, 1f, 0f, 1f);
        bb.addVertex(mat, -1f, 0f, 1f);
        BufferUploader.drawWithShader(bb.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableCull();
    }
}