package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class SphereRenderType extends RenderType {

    public static final Function<ResourceLocation, RenderType> STATIC_NOISE = Util.memoize((resourceLocation) -> {
        CompositeState compositeState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(ClientRegistry.SPHERE_SHADER::get))
                .setTextureState(new TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(NO_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
        return create("spherify", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false, compositeState);
    });

    public SphereRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}