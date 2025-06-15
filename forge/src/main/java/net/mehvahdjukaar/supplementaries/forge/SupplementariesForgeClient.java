package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.hud.forge.CannonChargeHudImpl;
import net.mehvahdjukaar.supplementaries.client.hud.forge.SelectableContainerItemHudImpl;
import net.mehvahdjukaar.supplementaries.client.hud.forge.SlimedOverlayHudImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredHeadLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.PartyHatLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.SlimedLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.AltimeterItemRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SupplementariesForgeClient {

    @SubscribeEvent
    public static void setup(final FMLClientSetupEvent event) {
        //  event.enqueueWork(ClientRegistry::setup);
        VibeChecker.checkVibe();
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
                renderer.addLayer(new PartyHatLayer.Generic(model, event.getEntityModels()));
            }
        }
        try {
            var skeletonRenderer = event.getRenderer(EntityType.SKELETON);
            if (skeletonRenderer != null) {
                skeletonRenderer.addLayer(new QuiverLayer(skeletonRenderer, true));
            }
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("Failed to add quiver layer to skeleton. This bug was caused by forge!");
        }
        try {
            var strayRenderer = event.getRenderer(EntityType.STRAY);
            if (strayRenderer != null) {
                strayRenderer.addLayer(new QuiverLayer(strayRenderer, true));
            }
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("Failed to add quiver layer to stray. This bug was caused by forge!");
        }


        //adds to all entities
        var entityTypes = ImmutableList.copyOf(
                ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(DefaultAttributes::hasSupplier)
                        .filter(e -> (e != EntityType.ENDER_DRAGON))
                        .map(entityType -> (EntityType<LivingEntity>) entityType)
                        .collect(Collectors.toList()));

        entityTypes.forEach((entityType -> {
            try {

                var r = event.getRenderer(entityType);
                if (r != null && !((Object) r instanceof NoopRenderer<?>)) r.addLayer(new SlimedLayer(r));
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("Failed to add slimed layer to entity: {}. This bug was caused by forge!", entityType);
            }
        }));


        try {
            var creeperRenderer = event.getRenderer(EntityType.CREEPER);
            if (creeperRenderer != null) {
                creeperRenderer.addLayer(new PartyHatLayer.Creeper(creeperRenderer, event.getEntityModels(), event.getContext().getItemInHandRenderer()));
            }
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("Failed to add party hat layer to creeper. This bug was caused by forge!");
        }
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
                (SelectableContainerItemHudImpl) SelectableContainerItemHudImpl.getInstance());

        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "cannon_charge_overlay",
                new CannonChargeHudImpl());

        event.registerBelow(VanillaGuiOverlay.FROSTBITE.id(), "slimed_overlay",
                new SlimedOverlayHudImpl());
    }


}
