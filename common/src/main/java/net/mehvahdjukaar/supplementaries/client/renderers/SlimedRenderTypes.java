package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mehvahdjukaar.supplementaries.SuppClientPlatformStuff;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class SlimedRenderTypes extends RenderType {

    public SlimedRenderTypes(String s, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean b, boolean b1, Runnable runnable, Runnable aSuper) {
        super(s, vertexFormat, mode, i, b, b1, runnable, aSuper);
    }

    private static class OffsetTexturing extends TexturingStateShard {
        public OffsetTexturing(int width, int height) {
            super("slime_offset_texturing",
                    () -> {
                        float u = (float) (System.currentTimeMillis() % 400000L) / 400000.0F;
                        Matrix4f translation = new Matrix4f().translation(0, -u, 0.0F);
                        float x = (float) width / 64;
                        float y = (float) height / 64;
                        translation.scale(x, y, 1);
                        RenderSystem.setTextureMatrix(translation);
                    },
                    RenderSystem::resetTextureMatrix);
        }
    }

    //will have few entries
    private static final Int2ObjectArrayMap<RenderType> TYPES = new Int2ObjectArrayMap<>();

    public static void clear() {
        TYPES.clear();
    }

    public static RenderType get(int width, int height) {
        return TYPES.computeIfAbsent((width << 16) | (height & 0xFFFF), k -> create("slimed",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false, true,
                CompositeState.builder()
                        .setTextureState(new TextureStateShard(ModTextures.SLIME_ENTITY_OVERLAY, false, false))
                        .setCullState(NO_CULL)
                        .setShaderState(new ShaderStateShard(ClientRegistry.ENTITY_OFFSET_SHADER))
                        .setOverlayState(OVERLAY)
                        .setLightmapState(LIGHTMAP)
                        .setDepthTestState(EQUAL_DEPTH_TEST)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTexturingState(new OffsetTexturing(width, height))
                        .createCompositeState(false)
        ));
    }

}
