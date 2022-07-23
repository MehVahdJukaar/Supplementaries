package net.mehvahdjukaar.supplementaries.common.world.data.map.client;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientHandler;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;

public class CMDclient {
    public static void init() {
        MapDecorationClientHandler.registerCustomRenderer(CMDreg.FLAG_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map/flag.png")));
        MapDecorationClientHandler.registerCustomRenderer(CMDreg.BANNER_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map/banner.png")));
    }
}
