package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;
import java.util.function.BiFunction;

public class GlowRenderType extends RenderType {

    public GlowRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));


    public static final RenderType GLOW_LINES = RenderType.create("glow_lines",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256,
            false, false,
            RenderType.CompositeState.builder().setLineState(THICK_LINES)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));



    static final BiFunction<ResourceLocation, CullStateShard, RenderType> OUTLINE = Util.memoize((texture, cull) ->
            RenderType.create("outline_depth",
                    DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,
                    false, false,
                    CompositeState.builder()
                            .setShaderState(RENDERTYPE_OUTLINE_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setCullState(cull)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setOutputState(OUTLINE_TARGET)
                            .createCompositeState(true)));

    public static RenderType outline(ResourceLocation pLocation) {
        return OUTLINE.apply(pLocation, NO_CULL);
    }

    public static RenderType getRenderType(ResourceLocation resourceLocation, boolean a, boolean b, boolean c) {

        return GlowRenderType.outline(resourceLocation);
    }
}
