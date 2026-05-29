package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mehvahdjukaar.supplementaries.client.renderers.DepthMaskUtils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs.PulleyMaskMode;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;


public class WindVaneBlockTileRenderer implements BlockEntityRenderer<WindVaneBlockTile> {

    private final ModelPart model;

    public WindVaneBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ClientRegistry.WIND_VANE_MODEL);
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
        PulleyMaskMode mode = ClientConfigs.Blocks.PULLEY_MASK_MODE.get();
        boolean masking = mode != PulleyMaskMode.OFF;

        if (masking) {
            // Flush any pending batch so nothing drawn before us is affected by the mask.
            if (buffers instanceof MultiBufferSource.BufferSource bs) bs.endBatch();
        }

        ps.pushPose();
        ps.translate(0.5, 1.0, 0.5);
        Matrix4f mat = new Matrix4f(ps.last().pose());

        // ── Pass 1: mask CUBE ─────────────────────────────────────────────────
        DepthMaskUtils.beginMask(mat, mode);

        // ── Pass 2: raw red quad ──────────────────────────────────────────────
        drawVisibleQuad(mat);

        // ── Pass 3: vanilla block via the normal pipeline ────────────────────
        ps.pushPose();
        ps.translate(-1.5, -0.5, 0.2);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.GLOWSTONE.defaultBlockState(), ps, buffers, light, overlay);
        ps.popPose();

        // ── Pass 4: item via the item renderer ───────────────────────────────
        ps.pushPose();
        ps.translate(0.5, -0.2, 0.2);
        ps.scale(1.5f, 1.5f, 1.5f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                Items.DIAMOND_SWORD.getDefaultInstance(),
                ItemDisplayContext.FIXED,
                light, overlay, ps, buffers, tile.getLevel(), 0);
        ps.popPose();

        if (masking) {
            // Flush the buffered block + item so they actually rasterize under the mask.
            if (buffers instanceof MultiBufferSource.BufferSource bs) bs.endBatch();
            DepthMaskUtils.endMask(mat, mode);
        }

        ps.popPose();
    }

    /** Horizontal red quad — half of it intersects the mask cube and gets hidden. */
    private static void drawVisibleQuad(Matrix4f mat) {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_CULL_FACE);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder bb = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bb.addVertex(mat, -1f, 0f, -1f).setColor(255, 32, 32, 255);
        bb.addVertex(mat,  1f, 0f, -1f).setColor(255, 32, 32, 255);
        bb.addVertex(mat,  1f, 0f,  1f).setColor(255, 32, 32, 255);
        bb.addVertex(mat, -1f, 0f,  1f).setColor(255, 32, 32, 255);
        BufferUploader.drawWithShader(bb.buildOrThrow());

        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
