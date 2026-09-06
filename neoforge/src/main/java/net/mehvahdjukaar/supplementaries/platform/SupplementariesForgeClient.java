package net.mehvahdjukaar.supplementaries.platform;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeHud;
import net.mehvahdjukaar.supplementaries.client.hud.SlimedOverlayHud;
import net.mehvahdjukaar.supplementaries.client.hud.platform.SelectableContainerItemHudImpl;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.EndermanSkullModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.SkullWithEyesModel;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpiderSkullBlock;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT)
public class SupplementariesForgeClient {

    @SubscribeEvent
    public static void setup(final FMLClientSetupEvent event) {
        //  event.enqueueWork(ClientRegistry::setup);
        VibeChecker.checkVibe();
        // Allocate 8 stencil bits on the main framebuffer. Required by DepthMaskUtils' STENCIL
        event.enqueueWork(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
    }

    @SubscribeEvent
    public static void onRegisterSkullModels(EntityRenderersEvent.CreateSkullModels event) {
        event.registerSkullModel(EndermanSkullBlock.TYPE,
                new EndermanSkullModel(event.getEntityModelSet().bakeLayer(ClientRegistry.ENDERMAN_HEAD_MODEL)));
        SkullBlockRenderer.SKIN_BY_TYPE.put(EndermanSkullBlock.TYPE,
                Supplementaries.res("textures/entity/enderman_head.png"));
        event.registerSkullModel(SpiderSkullBlock.TYPE,
                new SkullWithEyesModel(event.getEntityModelSet().bakeLayer(ClientRegistry.SPIDER_HEAD_MODEL), ModTextures.SPIDER_HEAD_EYES));
        SkullBlockRenderer.SKIN_BY_TYPE.put(SpiderSkullBlock.TYPE,
                Supplementaries.res("textures/entity/spider_head.png"));
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
