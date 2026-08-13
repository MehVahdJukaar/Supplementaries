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

// clips later geometry to one block cube. flush your buffer source before beginMask and endMask
// or unrelated stuff gets masked too.
// DEPTH is one cheap draw but leaves depth values around for the rest of the frame, so translucent
// chunk layers drawn after block entities can end up wrongly occluded.
// STENCIL doesn't touch depth but needs stencil bits on the framebuffer, see MainTargetMixin
public final class DepthMaskUtils {

    // anything nonzero works as long as nothing else in the frame writes the same stencil value
    private static final int STENCIL_REF = 0xFF;

    public static void beginMask(Matrix4f cubeMat, PulleyMaskMode mode) {
        switch (mode) {
            case OFF -> {
            }
            case DEPTH -> writeDepthMask(cubeMat);
            case STENCIL -> writeStencilMask(cubeMat);
        }
    }

    public static void endMask(Matrix4f cubeMat, PulleyMaskMode mode) {
        if (mode == PulleyMaskMode.STENCIL) clearStencilMask(cubeMat);
    }

    private static void writeDepthMask(Matrix4f mat) {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glColorMask(false, false, false, false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        drawCube(mat);
        GL11.glColorMask(true, true, true, true);
    }

    private static void writeStencilMask(Matrix4f mat) {
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

        GL11.glStencilFunc(GL11.GL_NOTEQUAL, STENCIL_REF, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00); // lock stencil so the masked draws don't mutate it
    }

    private static void clearStencilMask(Matrix4f mat) {
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
