package net.mehvahdjukaar.supplementaries.world.data.map.sup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecorationType;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapDecorationHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CMDreg {


    //with markers
    public static final CustomDecorationType<CustomDecoration, SignPostMarker> SIGN_POST_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "sign_post"), SignPostMarker::loadFromNBT, SignPostMarker::getFromWorld,CustomDecoration::new);
    public static final CustomDecorationType<CustomDecoration, BedMarker> BED_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "bed"), BedMarker::loadFromNBT, BedMarker::getFromWorld,CustomDecoration::new);
    public static final CustomDecorationType<FlagDecoration, FlagMarker> FLAG_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "flag"), FlagMarker::loadFromNBT, FlagMarker::getFromWorld,FlagDecoration::new);
    public static final CustomDecorationType<CustomDecoration, ConduitMarker> CONDUIT_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "conduit"), ConduitMarker::loadFromNBT, ConduitMarker::getFromWorld,CustomDecoration::new);
    public static final CustomDecorationType<CustomDecoration, RespawnAnchorMarker> RESPAWN_ANCHOR_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "respawn_anchor"), RespawnAnchorMarker::loadFromNBT, RespawnAnchorMarker::getFromWorld,CustomDecoration::new);
    public static final CustomDecorationType<CustomDecoration, LodestoneMarker> LODESTONE_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "lodestone"), LodestoneMarker::loadFromNBT, LodestoneMarker::getFromWorld,CustomDecoration::new);
    public static final CustomDecorationType<CustomDecoration, NetherPortalMarker> NETHER_PORTAL_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "nether_portal"), NetherPortalMarker::loadFromNBT, NetherPortalMarker::getFromWorld,CustomDecoration::new);
    public static final CustomDecorationType<CustomDecoration, EndPortalMarker> END_PORTAL_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("end_portal"), EndPortalMarker::loadFromNBT, EndPortalMarker::getFromWorld,CustomDecoration::new);
    //public static final CustomDecorationType<CustomDecoration, EndPortalMarker> END_CRYSTAL_DECORATION_TYPE = new CustomDecorationType<>(
    //        new ResourceLocation(MOD_ID, "end_crystal"), EndPortalMarker::loadFromNBT, EndPortalMarker::getFromWorld);
    public static final CustomDecorationType<CustomDecoration, EndGatewayMarker> END_GATEWAY_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "end_gateway"), EndGatewayMarker::loadFromNBT, EndGatewayMarker::getFromWorld,CustomDecoration::new);
    public static final CustomDecorationType<CustomDecoration, BeaconMarker> BEACON_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res( "beacon"), BeaconMarker::loadFromNBT, BeaconMarker::getFromWorld,CustomDecoration::new);

    private static CustomDecorationType<?,?> makeSimple(String id){
        return new CustomDecorationType<>(Supplementaries.res( id),CustomDecoration::new);
    }

    //simple for structures
    public static final CustomDecorationType<?,?> VILLAGE_TYPE = makeSimple( "village");
    public static final CustomDecorationType<?,?> JUNGLE_TEMPLE_TYPE = makeSimple( "jungle_temple");
    public static final CustomDecorationType<?,?> DESERT_PYRAMID_TYPE = makeSimple( "desert_pyramid");
    public static final CustomDecorationType<?,?> SHIPWRECK_TYPE = makeSimple( "shipwreck");
    public static final CustomDecorationType<?,?> RUINED_PORTAL_TYPE = makeSimple( "ruined_portal");
    public static final CustomDecorationType<?,?> IGLOO_TYPE = makeSimple( "igloo");
    public static final CustomDecorationType<?,?> END_CITY_TYPE = makeSimple( "end_city");
    public static final CustomDecorationType<?,?> NETHER_FORTRESS = makeSimple( "nether_fortress");
    public static final CustomDecorationType<?,?> PILLAGER_OUTPOST_TYPE = makeSimple( "pillager_outpost");
    public static final CustomDecorationType<?,?> BASTION_TYPE = makeSimple( "bastion");
    public static final CustomDecorationType<?,?> STRONGHOLD_TYPE = makeSimple( "stronghold");
    public static final CustomDecorationType<?,?> MINESHAFT_TYPE = makeSimple( "mineshaft");
    public static final CustomDecorationType<?,?> SWAMP_HUT_TYPE = makeSimple( "swamp_hut");

    public static void init(FMLCommonSetupEvent event){
        MapDecorationHandler.register(SIGN_POST_DECORATION_TYPE);
        MapDecorationHandler.register(BED_DECORATION_TYPE);
        MapDecorationHandler.register(FLAG_DECORATION_TYPE);
        MapDecorationHandler.register(CONDUIT_DECORATION_TYPE);
        MapDecorationHandler.register(RESPAWN_ANCHOR_DECORATION_TYPE);
        MapDecorationHandler.register(LODESTONE_DECORATION_TYPE);
        MapDecorationHandler.register(NETHER_PORTAL_DECORATION_TYPE);
        MapDecorationHandler.register(END_PORTAL_DECORATION_TYPE);
        MapDecorationHandler.register(END_GATEWAY_DECORATION_TYPE);
        MapDecorationHandler.register(BEACON_DECORATION_TYPE);


        MapDecorationHandler.register(VILLAGE_TYPE);
        MapDecorationHandler.register(JUNGLE_TEMPLE_TYPE);
        MapDecorationHandler.register(DESERT_PYRAMID_TYPE);
        MapDecorationHandler.register(SHIPWRECK_TYPE);
        MapDecorationHandler.register(RUINED_PORTAL_TYPE);
        MapDecorationHandler.register(IGLOO_TYPE);
        MapDecorationHandler.register(END_CITY_TYPE);
        MapDecorationHandler.register(NETHER_FORTRESS);
        MapDecorationHandler.register(PILLAGER_OUTPOST_TYPE);
        MapDecorationHandler.register(BASTION_TYPE);
        MapDecorationHandler.register(STRONGHOLD_TYPE);
        MapDecorationHandler.register(MINESHAFT_TYPE);
        MapDecorationHandler.register(SWAMP_HUT_TYPE);


    }

}
