package net.mehvahdjukaar.supplementaries.common.misc.map_markers.client;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientHandler;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;

public class ModMapMarkersClient {
    public static void init() {

        MapDecorationClientHandler.registerCustomRenderer(ModMapMarkers.FLAG_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("map_marker/flag")));
        MapDecorationClientHandler.registerCustomRenderer(ModMapMarkers.BANNER_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("map_marker/banner")));
    }
}
