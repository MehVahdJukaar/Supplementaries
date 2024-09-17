package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.core.mixins.forge.SelfExtraModelDataProvider;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.hud.forge.SelectableContainerItemHudImpl;
import net.mehvahdjukaar.supplementaries.client.hud.forge.SlimedOverlayHudImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredHeadLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.CreeperPartyHatLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.SlimedLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.forge.CannonChargeOverlayImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.items.AltimeterItemRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SupplementariesForgeClient {

    @SubscribeEvent
    public static void setup(final FMLClientSetupEvent event) {
        //  event.enqueueWork(ClientRegistry::setup);
        VibeChecker.checkVibe();
    }


    private static ShaderInstance staticNoiseShader;
    private static ShaderInstance entityOffsetShader;

    public static ShaderInstance getStaticNoiseShader() {
        return staticNoiseShader;
    }

    public static ShaderInstance getEntityOffsetShader() {
        return entityOffsetShader;
    }

    public static RenderType staticNoise(ResourceLocation location) {
        return RenderTypeAccessor.STATIC_NOISE.apply(location);
    }

    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) {
        try {
            ShaderInstance noiseShader = new ShaderInstance(event.getResourceProvider(),
                    Supplementaries.res("static_noise"), DefaultVertexFormat.NEW_ENTITY);

            event.registerShader(noiseShader, s -> staticNoiseShader = s);

            ShaderInstance slimeShader = new ShaderInstance(event.getResourceProvider(),
                    Supplementaries.res("entity_cutout_texture_offset"), DefaultVertexFormat.NEW_ENTITY);

            event.registerShader(slimeShader, s -> entityOffsetShader = s);

        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed to parse shader: " + e);
        }
    }

    @SubscribeEvent
    public static void onRegisterSkullModels(EntityRenderersEvent.CreateSkullModels event) {
        event.registerSkullModel(EndermanSkullBlock.TYPE,
                new SkullModel(event.getEntityModelSet().bakeLayer(ModelLayers.SKELETON_SKULL)));
        SkullBlockRenderer.SKIN_BY_TYPE.put(EndermanSkullBlock.TYPE,
                Supplementaries.res("textures/entity/enderman_head.png"));
    }


    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);
            if (renderer != null) {
                renderer.addLayer(new QuiverLayer(renderer, false));
                RenderLayerParent model = renderer;
                renderer.addLayer(new JarredHeadLayer<>(model, event.getEntityModels()));
            }
        }
        var skeletonRenderer = event.getRenderer(EntityType.SKELETON);
        if (skeletonRenderer != null) {
            skeletonRenderer.addLayer(new QuiverLayer(skeletonRenderer, true));
        }
        var strayRenderer = event.getRenderer(EntityType.STRAY);
        if (strayRenderer != null) {
            strayRenderer.addLayer(new QuiverLayer(strayRenderer, true));
        }


        //adds to all entities
        var entityTypes = ImmutableList.copyOf(
                ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(DefaultAttributes::hasSupplier)
                        .filter(e -> (e != EntityType.ENDER_DRAGON))
                        .map(entityType -> (EntityType<LivingEntity>) entityType)
                        .collect(Collectors.toList()));

        try {
            entityTypes.forEach((entityType -> {
                var r = event.getRenderer(entityType);
                if (r != null && !((Object) r instanceof NoopRenderer<?>)) r.addLayer(new SlimedLayer(r));
            }));
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed to add slimed layer to entities:   ", e);
        }

        var creeperRenderer = event.getRenderer(EntityType.CREEPER);
        creeperRenderer.addLayer(new CreeperPartyHatLayer(creeperRenderer, event.getEntityModels()));

        //player skins
        for (String skinType : event.getSkins()) {
            var r = event.getSkin(skinType);
            if (r != null) r.addLayer(new SlimedLayer(r));
        }
    }


    @SubscribeEvent
    public static void onPackReload(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            AltimeterItemRenderer.onReload();
        }
    }

    @SubscribeEvent
    public static void onAddGuiLayers(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "quiver_overlay",
                (SelectableContainerItemHudImpl) SelectableContainerItemHudImpl.INSTANCE);

        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "cannon_charge_overlay",
                new CannonChargeOverlayImpl());

        event.registerBelow(VanillaGuiOverlay.FROSTBITE.id(), "slimed_overlay",
                new SlimedOverlayHudImpl());
    }


    private abstract static class RenderTypeAccessor extends RenderType {
        protected static final ShaderStateShard STATIC_NOISE_SHARD = new ShaderStateShard(SupplementariesForgeClient::getStaticNoiseShader);

        static final Function<ResourceLocation, RenderType> STATIC_NOISE = Util.memoize((resourceLocation) -> {
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

}
