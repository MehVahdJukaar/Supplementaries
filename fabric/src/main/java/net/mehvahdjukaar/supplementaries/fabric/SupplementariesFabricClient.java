package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.DifferentProspectiveItemRenderer;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.gui.screens.TitleScreen;

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
        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.FLUTE_ITEM.get(),
                new DifferentProspectiveItemRenderer(ClientRegistry.FLUTE_2D_MODEL, ClientRegistry.FLUTE_3D_MODEL));
        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.QUIVER_ITEM.get(),
                new DifferentProspectiveItemRenderer(ClientRegistry.QUIVER_2D_MODEL, ClientRegistry.QUIVER_3D_MODEL));
    }


}
