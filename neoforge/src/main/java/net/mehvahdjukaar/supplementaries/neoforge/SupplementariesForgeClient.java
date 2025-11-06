package net.mehvahdjukaar.supplementaries.neoforge;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.moonlight.api.platform.neoforge.ForgeHelperImpl;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeHud;
import net.mehvahdjukaar.supplementaries.client.hud.SlimedOverlayHud;
import net.mehvahdjukaar.supplementaries.client.hud.neoforge.SelectableContainerItemHudImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredHeadLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.PartyHatLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.SlimedLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.AltimeterItemRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpiderSkullBlock;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.stream.Collectors;

@EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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
        event.registerSkullModel(SpiderSkullBlock.TYPE,
                new SkullModel(event.getEntityModelSet().bakeLayer(ModelLayers.SKELETON_SKULL)));
        SkullBlockRenderer.SKIN_BY_TYPE.put(SpiderSkullBlock.TYPE,
                Supplementaries.res("textures/entity/spider_head.png"));
    }


    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);
            if (renderer instanceof LivingEntityRenderer<?, ?> le) {
                le.addLayer(new QuiverLayer(le, false));
                le.addLayer(new JarredHeadLayer<>((RenderLayerParent) le, event.getEntityModels()));
                le.addLayer(new PartyHatLayer.Generic(le, event.getEntityModels()));
                le.addLayer(new SlimedLayer(le));
            }
        }

        var skeletonRenderer = event.getRenderer(EntityType.SKELETON);
        if (skeletonRenderer instanceof LivingEntityRenderer<?, ?> le) {
            le.addLayer(new QuiverLayer(le, true));
        }
        var strayRenderer = event.getRenderer(EntityType.STRAY);
        if (strayRenderer instanceof LivingEntityRenderer<?, ?> le) {
            le.addLayer(new QuiverLayer(le, true));
        }
        var creeperRenderer = event.getRenderer(EntityType.CREEPER);
        if (creeperRenderer instanceof LivingEntityRenderer<?, ?> le) {
            le.addLayer(new PartyHatLayer.Creeper(le, event.getEntityModels(), event.getContext().getItemInHandRenderer()));
        }

        //adds to all entities
        var entityTypes = ImmutableList.copyOf(
                BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(DefaultAttributes::hasSupplier)
                        .filter(e -> (e != EntityType.ENDER_DRAGON))
                        .map(entityType -> (EntityType<LivingEntity>) entityType)
                        .collect(Collectors.toList()));

        try {
            entityTypes.forEach((entityType -> {
                var r = event.getRenderer(entityType);
                if (r instanceof LivingEntityRenderer<?, ?> le) {
                    le.addLayer(new SlimedLayer(le));
                }
            }));
        } catch (Exception e) {
            Supplementaries.LOGGER.error("Failed to add slimed layer to entities:   ", e);
        }
    }

    @SubscribeEvent
    public static void onAddGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Supplementaries.res("selectable_container_item"),
                SelectableContainerItemHudImpl.getInstance());

        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, Supplementaries.res("cannon_charge"),
                CannonChargeHud.INSTANCE);

        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, Supplementaries.res("slimed"),
                SlimedOverlayHud.INSTANCE);
    }


}
