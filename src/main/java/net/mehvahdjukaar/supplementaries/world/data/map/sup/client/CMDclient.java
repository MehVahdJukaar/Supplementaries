package net.mehvahdjukaar.supplementaries.world.data.map.sup.client;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.client.MapDecorationClient;
import net.mehvahdjukaar.supplementaries.world.data.map.sup.CMDreg;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CMDclient {
    public static void init(FMLClientSetupEvent event){
        MapDecorationClient.bindDecorationRenderer(CMDreg.FLAG_DECORATION_TYPE, new FlagDecorationRenderer(Textures.FLAG_MARKER_TEXTURE));
        MapDecorationClient.bindSimpleRenderer(CMDreg.BED_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.LODESTONE_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.NETHER_PORTAL_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.END_PORTAL_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.END_GATEWAY_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.RESPAWN_ANCHOR_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.BEACON_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.CONDUIT_DECORATION_TYPE);
        MapDecorationClient.bindSimpleRenderer(CMDreg.SIGN_POST_DECORATION_TYPE);


    }
}
