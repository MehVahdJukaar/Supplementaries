package net.mehvahdjukaar.supplementaries.common.world.data.map.client;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientHandler;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.data.map.ModMapMarkers;

public class CMDclient {
    public static void init() {
        MapDecorationClientHandler.registerCustomRenderer(ModMapMarkers.FLAG_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map/flag.png")));
        MapDecorationClientHandler.registerCustomRenderer(ModMapMarkers.BANNER_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map/banner.png")));
    }
}
