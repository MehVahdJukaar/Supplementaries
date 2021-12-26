package net.mehvahdjukaar.supplementaries.common.world.data.map.client;

import net.mehvahdjukaar.selene.map.client.DecorationRenderer;
import net.mehvahdjukaar.selene.map.client.MapDecorationRenderHandler;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CMDclient {
    public static void init(FMLClientSetupEvent event){
        MapDecorationRenderHandler.bindDecorationRenderer(CMDreg.FLAG_DECORATION_TYPE, new ColoredDecorationRenderer(Textures.FLAG_MARKER_TEXTURE));
        MapDecorationRenderHandler.bindDecorationRenderer(CMDreg.BANNER_DECORATION_TYPE, new ColoredDecorationRenderer(Textures.BANNER_MARKER_TEXTURE));
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.BED_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.LODESTONE_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.NETHER_PORTAL_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.END_PORTAL_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.END_GATEWAY_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.RESPAWN_ANCHOR_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.BEACON_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.CONDUIT_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.SIGN_POST_DECORATION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.CHEST_DECORATION_TYPE);


        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.VILLAGE_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.JUNGLE_TEMPLE_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.DESERT_PYRAMID_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.SHIPWRECK_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.RUINED_PORTAL_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.IGLOO_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.END_CITY_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.NETHER_FORTRESS);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.PILLAGER_OUTPOST_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.BASTION_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.STRONGHOLD_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.MINESHAFT_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.SWAMP_HUT_TYPE);
        MapDecorationRenderHandler.bindSimpleRenderer(CMDreg.OCEAN_RUIN_TYPE);


    }
}
