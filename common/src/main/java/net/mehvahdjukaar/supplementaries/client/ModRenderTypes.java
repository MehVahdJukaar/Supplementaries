package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
    }

    public static final RenderType CANNON_TRAJECTORY = makeRenderType(ModTextures.CANNON_TRAJECTORY);
    public static final RenderType CANNON_TRAJECTORY_RED = makeRenderType(ModTextures.CANNON_TRAJECTORY_RED);

    private static RenderType makeRenderType(ResourceLocation cannonTrajectoryRed) {
        return create("supplementaries_texture_strip",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS, 256,
                CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(cannonTrajectoryRed, false, false))
                        .setCullState(NO_CULL)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(false));
    }
}
