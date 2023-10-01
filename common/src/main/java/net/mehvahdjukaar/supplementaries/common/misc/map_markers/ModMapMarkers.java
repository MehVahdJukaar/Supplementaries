package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.DummyMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.CustomDecorationType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers.*;

public class ModMapMarkers {


    //builtin code defined ones

    //with markers
    public static final CustomDecorationType<CustomMapDecoration, SignPostMarker> SIGN_POST_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("sign_post"), SignPostMarker::new, SignPostMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, BedMarker> BED_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("bed"), BedMarker::new, BedMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<ColoredDecoration, FlagMarker> FLAG_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("flag"), FlagMarker::new, FlagMarker::getFromWorld, ColoredDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, NetherPortalMarker> NETHER_PORTAL_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("nether_portal"), NetherPortalMarker::new, NetherPortalMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, BeaconMarker> BEACON_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("beacon"), BeaconMarker::new, BeaconMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<ColoredDecoration, CeilingBannerMarker> BANNER_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("banner"), CeilingBannerMarker::new, CeilingBannerMarker::getFromWorld, ColoredDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, ChestMarker> CHEST_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("chest"), ChestMarker::new, ChestMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, WaystoneMarker> WAYSTONE_DECORATION_TYPE = new CustomDecorationType<>(
            Supplementaries.res("waystone"), WaystoneMarker::new, WaystoneMarker::getFromWorld, CustomMapDecoration::new);

    public static final CustomDecorationType<CustomMapDecoration, ?> DEATH_MARKER = CustomDecorationType.dynamic(
            Supplementaries.res("death_marker"),  CustomMapDecoration::new, DeathMarker::getForPlayer);


    public static void init() {
        MapDecorationRegistry.registerCustomType(SIGN_POST_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(BED_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(FLAG_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(NETHER_PORTAL_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(BEACON_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(BANNER_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(CHEST_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(WAYSTONE_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(DEATH_MARKER);
    }

}
