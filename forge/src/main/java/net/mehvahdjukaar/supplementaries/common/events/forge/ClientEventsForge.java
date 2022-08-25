package net.mehvahdjukaar.supplementaries.common.events.forge;

import net.mehvahdjukaar.supplementaries.SupplementariesClient;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.QuiverLayer;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.api.IQuarkButtonAllowed;
import vazkii.quark.base.client.config.screen.QuarkConfigHomeScreen;

public class ClientEventsForge {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ClientEventsForge.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEventsForge::onAddLayers);
    }

    @SubscribeEvent
    public static void itemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() != null) {
            ClientEvents.onItemTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
        }
    }

    @SubscribeEvent
    public static void screenInit(ScreenEvent.Init.Post event) {
        if(CompatHandler.configured) {
            ClientEvents.addConfigButton(event.getScreen(), event.getListenersList(), event::addListener);
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) {
            ClientEvents.onClientTick(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SupplementariesClient.onRenderTick(event.renderTickTime);
        }
    }

    //forge only below

    //TODO: add to fabric

    private static double wobble; // from 0 to 1

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Player p = Minecraft.getInstance().player;
        if (p != null && !Minecraft.getInstance().isPaused()) {
            boolean isOnRope = ClientEvents.isIsOnRope();
            if (isOnRope || wobble != 0) {
                double period = ClientConfigs.Blocks.ROPE_WOBBLE_PERIOD.get();
                double newWobble = (((p.tickCount + event.getPartialTick()) / period) % 1);
                if (!isOnRope && newWobble < wobble) {
                    wobble = 0;
                } else {
                    wobble = newWobble;
                }
                event.setRoll((float) (event.getRoll() + Mth.sin((float) (wobble * 2 * Math.PI)) * ClientConfigs.Blocks.ROPE_WOBBLE_AMPLITUDE.get()));
            }
        }
    }


    public static void onAddLayers(EntityRenderersEvent.AddLayers event){
        for (String skinType : event.getSkins()){
            var renderer = event.getSkin(skinType);
            addPlayerLayer(renderer);
        }
    }

    private static <T extends LivingEntity, M extends HierarchicalModel<T>, R extends LivingEntityRenderer<T, M>> void addLayer(
            @Nullable R renderer) {
        if (renderer != null) {
            renderer.addLayer(new QuiverLayer(renderer));
        }
    }
    private static <M extends EntityModel<? extends Player>, R extends LivingEntityRenderer<? extends Player, M>> void addPlayerLayer(@Nullable R renderer){
        if(renderer != null) {
            renderer.addLayer(new QuiverLayer(renderer));
        }
    }
}
