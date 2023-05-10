package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import org.joml.Matrix4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class SlimedRenderType extends RenderType {

    public SlimedRenderType(String s, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean b, boolean b1, Runnable runnable, Runnable aSuper) {
        super(s, vertexFormat, mode, i, b, b1, runnable, aSuper);
    }

    protected static final TexturingStateShard TEXTURING_STATE_SHARD = new TexturingStateShard("entity_glint_texturing", () -> {
        animateTexture(1.2F, 4L);

    }, RenderSystem::resetTextureMatrix);

    protected static final TextureStateShard TEXTURE_SHARD = new TextureStateShard(ModTextures.SLIME_ENTITY_OVERLAY, true, false);
/*
    public static final RenderType SLIMED_RENDER_TYPE =
            create("slimed", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                    true, true, CompositeState.builder()
                            .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                            .setTextureState(TEXTURE_SHARD)
                            .setCullState(NO_CULL)
                            .setOverlayState(OVERLAY)
                            .setLightmapState(LIGHTMAP)
                            .setDepthTestState(EQUAL_DEPTH_TEST)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setTexturingState(TEXTURING_STATE_SHARD)
                            .createCompositeState(true));
*/

    private static void animateTexture(float in, long time) {
        long i = Util.getMillis() * time;
        float f = (float) (i % 80000L) / 80000.0F;
        float f1 = 0.5f + Mth.sin((float) (((float) (i % 30000L) / 30000.0F) * Math.PI)) * 0.5f;

        Matrix4f matrix4f = (new Matrix4f()).translation(0.0F, f, 0.0F);

        matrix4f.rotate(Axis.ZP.rotationDegrees(30));

        matrix4f.mul((new Matrix4f()).scale(0.5f, 0.5f, 0.5f));
        RenderSystem.setTextureMatrix(matrix4f);


    }

}
