package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.CustomDecorationType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers.*;
import net.minecraft.resources.ResourceLocation;

public class ModMapMarkers {


    //with markers
    public static final CustomDecorationType<CustomMapDecoration, SignPostMarker> SIGN_POST_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("sign_post"), SignPostMarker::new, SignPostMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, BedMarker> BED_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("bed"), BedMarker::new, BedMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<ColoredDecoration, FlagMarker> FLAG_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("flag"), FlagMarker::new, FlagMarker::getFromWorld, ColoredDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, NetherPortalMarker> NETHER_PORTAL_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("nether_portal"), NetherPortalMarker::new, NetherPortalMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, EndPortalMarker> END_PORTAL_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("end_portal"), EndPortalMarker::new, EndPortalMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, BeaconMarker> BEACON_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("beacon"), BeaconMarker::new, BeaconMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<ColoredDecoration, CeilingBannerMarker> BANNER_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("banner"), CeilingBannerMarker::new, CeilingBannerMarker::getFromWorld, ColoredDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, ChestMarker> CHEST_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("chest"), ChestMarker::new, ChestMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, WaystoneMarker> WAYSTONE_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("waystone"), WaystoneMarker::new, WaystoneMarker::getFromWorld, CustomMapDecoration::new);

    //  public static final CustomDecorationType<CustomMapDecoration, ConduitMarker> CONDUIT_DECORATION_TYPE = new CustomDecorationType<>(
    //          Supplementaries.res("conduit"), ConduitMarker::new, ConduitMarker::getFromWorld, CustomMapDecoration::new);
    // public static final CustomDecorationType<CustomMapDecoration, RespawnAnchorMarker> RESPAWN_ANCHOR_DECORATION_TYPE = new CustomDecorationType<>(
    //          Supplementaries.res("respawn_anchor"), RespawnAnchorMarker::new, RespawnAnchorMarker::getFromWorld, CustomMapDecoration::new);
    //public static final CustomDecorationType<CustomMapDecoration, LodestoneMarker> LODESTONE_DECORATION_TYPE = new CustomDecorationType<>(
    //        Supplementaries.res("lodestone"), LodestoneMarker::new, LodestoneMarker::getFromWorld, CustomMapDecoration::new);
    //public static final CustomDecorationType<CustomMapDecoration, EndGatewayMarker> END_GATEWAY_DECORATION_TYPE = new CustomDecorationType<>(
    //        Supplementaries.res("end_gateway"), EndGatewayMarker::new, EndGatewayMarker::getFromWorld, CustomMapDecoration::new);


    //simple for structures. Handled via datapack
    public static final ResourceLocation VILLAGE_TYPE = Supplementaries.res("village");
    public static final ResourceLocation JUNGLE_TEMPLE_TYPE = Supplementaries.res("jungle_temple");
    public static final ResourceLocation DESERT_PYRAMID_TYPE = Supplementaries.res("desert_pyramid");
    public static final ResourceLocation SHIPWRECK_TYPE = Supplementaries.res("shipwreck");
    public static final ResourceLocation RUINED_PORTAL_TYPE = Supplementaries.res("ruined_portal");
    public static final ResourceLocation IGLOO_TYPE = Supplementaries.res("igloo");
    public static final ResourceLocation END_CITY_TYPE = Supplementaries.res("end_city");
    public static final ResourceLocation NETHER_FORTRESS = Supplementaries.res("nether_fortress");
    public static final ResourceLocation PILLAGER_OUTPOST_TYPE = Supplementaries.res("pillager_outpost");
    public static final ResourceLocation BASTION_TYPE = Supplementaries.res("bastion");
    public static final ResourceLocation STRONGHOLD_TYPE = Supplementaries.res("stronghold");
    public static final ResourceLocation MINESHAFT_TYPE = Supplementaries.res("mineshaft");
    public static final ResourceLocation SWAMP_HUT_TYPE = Supplementaries.res("swamp_hut");
    public static final ResourceLocation OCEAN_RUIN_TYPE = Supplementaries.res("ocean_ruin");

    public static void init() {
        reg(SIGN_POST_DECORATION_TYPE);
        reg(BED_DECORATION_TYPE);
        reg(FLAG_DECORATION_TYPE);
        reg(NETHER_PORTAL_DECORATION_TYPE);
        reg(BEACON_DECORATION_TYPE);
        reg(BANNER_DECORATION_TYPE);
        reg(CHEST_DECORATION_TYPE);
        reg(WAYSTONE_DECORATION_TYPE);
        reg(END_PORTAL_DECORATION_TYPE);
    }

    private static void reg(CustomDecorationType<?, ?> type) {
        MapDecorationRegistry.register(type.getCustomFactoryID(), () -> type);
    }

}
