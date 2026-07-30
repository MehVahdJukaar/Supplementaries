package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs.PulleyMaskMode;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * 1-block cube-mask helpers for "clipping" later-drawn geometry to a chosen volume.
 *
 * <p>Two strategies are supported (see {@link PulleyMaskMode}):
 * <ul>
 *   <li><b>DEPTH</b>: writes the cube into the depth buffer only. Anything drawn afterwards whose
 *       depth fails {@code GL_LEQUAL} at the same pixel is hidden. Cheap; one draw call. Downside:
 *       the depth values persist for the rest of the frame, so translucent chunk layers (slime,
 *       glass, water) drawn after BE rendering can be wrongly occluded.</li>
 *   <li><b>STENCIL</b>: writes the cube into the stencil buffer with a sentinel ref value. The
 *       subsequent masked draws use {@code GL_NOTEQUAL} against that ref. After the draws complete,
 *       the cube is re-rendered with stencil-op=REPLACE 0 to zero out the region. Doesn't touch
 *       the depth buffer, so other translucent rendering is unaffected. Requires the framebuffer
 *       to have stencil bits (see {@code MainTargetMixin}).</li>
 * </ul>
 *
 * <p>Typical call pattern (caller flushes the buffer source around the masked draws so unrelated
 * geometry isn't affected):
 * <pre>{@code
 *   PulleyMaskMode mode = ClientConfigs.Blocks.PULLEY_MASK_MODE.get();
 *   if (mode != OFF) {
 *       bufferSource.endBatch();
 *       DepthMaskUtils.beginMask(cubeMat, mode);
 *   }
 *   // queue masked geometry to bufferSource
 *   if (mode != OFF) {
 *       bufferSource.endBatch(); // rasterize the masked geometry against our mask
 *       DepthMaskUtils.endMask(cubeMat, mode);
 *   }
 * }</pre>
 */
public final class DepthMaskUtils {

    /**
     * Sentinel stencil value written by the mask. Picked arbitrarily — anything nonzero works
     * provided nothing else in the frame writes the same value to stencil.
     */
    private static final int STENCIL_REF = 0xFF;

    /**
     * Writes the mask cube and configures GL so subsequent draws are clipped against it.
     * No-op when {@code mode == OFF}.
     */
    public static void beginMask(Matrix4f cubeMat, PulleyMaskMode mode) {
        switch (mode) {
            case OFF -> { /* no-op */ }
            case DEPTH -> writeDepthMask(cubeMat);
            case STENCIL -> writeStencilMask(cubeMat);
        }
    }

    /**
     * Tears down state set by {@link #beginMask}. For {@code STENCIL} mode, also zeros out the
     * stencil values we wrote so they don't pollute later frames or other stencil consumers.
     * No-op for {@code DEPTH} and {@code OFF}.
     */
    public static void endMask(Matrix4f cubeMat, PulleyMaskMode mode) {
        if (mode == PulleyMaskMode.STENCIL) clearStencilMask(cubeMat);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DEPTH mode
    // ─────────────────────────────────────────────────────────────────────────

    private static void writeDepthMask(Matrix4f mat) {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glColorMask(false, false, false, false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        drawCube(mat);
        GL11.glColorMask(true, true, true, true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STENCIL mode
    // ─────────────────────────────────────────────────────────────────────────

    private static void writeStencilMask(Matrix4f mat) {
        // Pass 1: write STENCIL_REF where the cube draws. No colour, no depth — orthogonal
        // to whatever's already in the depth buffer.
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_ALWAYS, STENCIL_REF, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        drawCube(mat);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);

        // Configure subsequent draws to pass only where stencil != REF.
        GL11.glStencilFunc(GL11.GL_NOTEQUAL, STENCIL_REF, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00); // lock stencil so the masked draws don't mutate it
    }

    private static void clearStencilMask(Matrix4f mat) {
        // Re-draw the cube writing stencil=0, then turn the test off.
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        drawCube(mat);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geometry: 1-block axis-aligned cube centred at the origin of {@code mat}.
    // ─────────────────────────────────────────────────────────────────────────

    private static void drawCube(Matrix4f mat) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder bb = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        final float n = -0.5f, p = 0.5f;
        bb.addVertex(mat, n, n, n); bb.addVertex(mat, n, p, n); bb.addVertex(mat, p, p, n); bb.addVertex(mat, p, n, n);
        bb.addVertex(mat, n, n, p); bb.addVertex(mat, p, n, p); bb.addVertex(mat, p, p, p); bb.addVertex(mat, n, p, p);
        bb.addVertex(mat, n, n, n); bb.addVertex(mat, n, n, p); bb.addVertex(mat, n, p, p); bb.addVertex(mat, n, p, n);
        bb.addVertex(mat, p, n, n); bb.addVertex(mat, p, p, n); bb.addVertex(mat, p, p, p); bb.addVertex(mat, p, n, p);
        bb.addVertex(mat, n, n, n); bb.addVertex(mat, p, n, n); bb.addVertex(mat, p, n, p); bb.addVertex(mat, n, n, p);
        bb.addVertex(mat, n, p, n); bb.addVertex(mat, n, p, p); bb.addVertex(mat, p, p, p); bb.addVertex(mat, p, p, n);
        BufferUploader.drawWithShader(bb.buildOrThrow());
    }
}
