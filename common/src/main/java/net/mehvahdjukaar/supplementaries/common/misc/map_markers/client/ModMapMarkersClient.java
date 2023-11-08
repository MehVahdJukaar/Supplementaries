package net.mehvahdjukaar.supplementaries.common.misc.map_markers.client;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientHandler;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;

public class ModMapMarkersClient {
    public static void init() {

        MapDecorationClientHandler.registerCustomRenderer(ModMapMarkers.FLAG_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map_markers/flag.png")));
        MapDecorationClientHandler.registerCustomRenderer(ModMapMarkers.BANNER_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map_markers/banner.png")));
    }
}
