package net.mehvahdjukaar.supplementaries.forge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SupplementariesForgeClient {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        //  event.enqueueWork(ClientRegistry::setup);

        crashIfOptifineHasNukedForge();
    }


    private static ShaderInstance staticNoiseShader;

    public static ShaderInstance getStaticNoiseShader() {
        return staticNoiseShader;
    }

    public static RenderType staticNoise(ResourceLocation location) {
        return RenderTypeAccessor.STATIC_NOISE.apply(location);
    }

    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) {
        try {
            ShaderInstance translucentParticleShader = new ShaderInstance(event.getResourceManager(),
                    Supplementaries.res("static_noise"), DefaultVertexFormat.NEW_ENTITY);

            event.registerShader(translucentParticleShader, s -> staticNoiseShader = s);

        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed to parse shader: " + e);
        }
    }

    private abstract static class RenderTypeAccessor extends RenderType {
        protected static final ShaderStateShard STATIC_NOISE_SHARD = new ShaderStateShard(SupplementariesForgeClient::getStaticNoiseShader);
        static Function<ResourceLocation, RenderType> STATIC_NOISE = Util.memoize((resourceLocation) -> {
            CompositeState compositeState = RenderType.CompositeState.builder()
                    .setShaderState(STATIC_NOISE_SHARD)
                    .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return create("static_noise", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 256, true, false, compositeState);
        });

        public RenderTypeAccessor(String string, VertexFormat arg, VertexFormat.Mode arg2, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
            super(string, arg, arg2, i, bl, bl2, runnable, runnable2);
        }
    }

    @SuppressWarnings("all")
    private static void crashIfOptifineHasNukedForge() {
        if (PlatformHelper.isModLoaded("optifinefixer")) return;
        try {
            new BakedQuad(new int[]{}, 0, Direction.UP, null, true, false);
        } catch (Exception e) {
            if(e instanceof NoSuchMethodException){
                throw new VibeCheck.ModErrorException("Your Optifine version is incompatible with Forge. Refusing to continue further", e);
            }
        }
    }

}
