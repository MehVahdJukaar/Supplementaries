package net.mehvahdjukaar.supplementaries.common.world.data.map.client;

import net.mehvahdjukaar.selene.map.client.MapDecorationRenderHandler;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CMDclient {
    public static void init(FMLClientSetupEvent event) {
        MapDecorationRenderHandler.bindDecorationRenderer(CMDreg.FLAG_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map/flag.png")));
        MapDecorationRenderHandler.bindDecorationRenderer(CMDreg.BANNER_DECORATION_TYPE, new ColoredDecorationRenderer(Supplementaries.res("textures/map/banner.png")));
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.BED_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.NETHER_PORTAL_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.END_PORTAL_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.BEACON_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.SIGN_POST_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.CHEST_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.WAYSTONE_DECORATION_TYPE);

        //MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.LODESTONE_DECORATION_TYPE);
        //MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.CONDUIT_DECORATION_TYPE);
        //MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.END_GATEWAY_DECORATION_TYPE);
        //MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.RESPAWN_ANCHOR_DECORATION_TYPE);
    }
}
