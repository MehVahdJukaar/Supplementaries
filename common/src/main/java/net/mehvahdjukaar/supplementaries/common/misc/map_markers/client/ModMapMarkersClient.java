package net.mehvahdjukaar.supplementaries.common.misc.map_markers.client;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientManager;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;

public class ModMapMarkersClient {

    public static void init() {
        MapDecorationClientManager.registerCustomRenderer(ModMapMarkers.FLAG_FACTORY_ID, ColoredDecorationRenderer::new);
        MapDecorationClientManager.registerCustomRenderer(ModMapMarkers.BED_FACTORY_ID, ColoredDecorationRenderer::new);
        MapDecorationClientManager.registerCustomRenderer(ModMapMarkers.BANNER_FACTORY_ID, ColoredDecorationRenderer::new);
    }
}
