package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.DifferentProspectiveItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.LumiseneFluidRenderPropertiesImpl;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Set;

public class SupplementariesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
    }

    private static boolean firstScreenShown = false;

    public static void init() {
        ClientRegistry.init();
        ClientHelper.addClientSetup(SupplementariesFabricClient::fabricSetup);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!firstScreenShown && screen instanceof TitleScreen) {
                ClientEvents.onFirstScreen(screen);
                firstScreenShown = true;
            }
        });
    }

    private static void fabricSetup() {
        Set<ItemDisplayContext> set = Set.of(ItemDisplayContext.GUI, ItemDisplayContext.GROUND, ItemDisplayContext.FIXED);

        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.FLUTE_ITEM.get(),
                new DifferentProspectiveItemRenderer(ClientRegistry.FLUTE_2D_MODEL, ClientRegistry.FLUTE_3D_MODEL, set));
        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.QUIVER_ITEM.get(),
                new DifferentProspectiveItemRenderer(ClientRegistry.QUIVER_2D_MODEL, ClientRegistry.QUIVER_3D_MODEL, set));
        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.CONFETTI_POPPER.get(),
                new DifferentProspectiveItemRenderer(ClientRegistry.POPPER_HEAD_MODEL, ClientRegistry.POPPER_GUI_MODEL,
                        Set.of(ItemDisplayContext.HEAD)));

        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.LUMISENE_FLUID.get(), new LumiseneFluidRenderPropertiesImpl());

    }


}
