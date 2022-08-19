package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class SlimedRenderType extends RenderType {

    public SlimedRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
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
        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(0.0F, f, 0.0F);
        matrix4f.multiply(Vector3f.ZP.rotationDegrees(30));
        matrix4f.multiply(Matrix4f.createScaleMatrix(0.5f, 0.5f, 0.5f));
        RenderSystem.setTextureMatrix(matrix4f);


    }

}
