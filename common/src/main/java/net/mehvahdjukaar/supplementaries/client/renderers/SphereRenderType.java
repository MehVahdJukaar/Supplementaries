package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class SphereRenderType extends RenderType {

    /**
     * A byte-for-byte copy of {@code DefaultVertexFormat.NEW_ENTITY}, kept as its own instance on purpose.
     * Iris matches vertex formats by reference identity and, whenever a shaderpack is active, transparently
     * swaps any buffer using the real {@code NEW_ENTITY} for its own extended layout, overwriting the
     * "Normal" attribute with a recomputed face normal on every quad. This shader repurposes that attribute
     * to carry the sphere's center offset, so getting rewritten breaks it; using a distinct format instance
     * that isn't `==` to the vanilla one keeps Iris from touching it at all.
     */
    private static final VertexFormat SPHERE_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("UV1", VertexFormatElement.UV1)
            .add("UV2", VertexFormatElement.UV2)
            .add("Normal", VertexFormatElement.NORMAL)
            .padding(1)
            .build();

    public static final Function<ResourceLocation, RenderType> RENDER_TYPE = Util.memoize((resourceLocation) -> {
        CompositeState compositeState = CompositeState.builder()
                .setShaderState(new ShaderStateShard(ClientRegistry.SPHERE_SHADER))
                .setTextureState(new TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setTexturingState(new TexturingStateShard("sphere_texturing",
                        () -> {
                            ShaderInstance shader = ClientRegistry.SPHERE_SHADER.get();
                            shader.safeGetUniform("SphereTexture").set(0);
                        },
                        () -> {
                        }))
                .createCompositeState(true);
        return create("spherify", SPHERE_FORMAT,
                VertexFormat.Mode.QUADS, 256, true, false, compositeState);
    });

    public SphereRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}