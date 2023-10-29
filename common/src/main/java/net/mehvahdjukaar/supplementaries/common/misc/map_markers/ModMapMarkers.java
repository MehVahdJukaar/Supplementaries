package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.CustomDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers.*;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Set;

public class ModMapMarkers {


    //builtin code defined ones

    //with markers
    public static final CustomDecorationType<CustomMapDecoration, SignPostMarker> SIGN_POST_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("sign_post"), SignPostMarker::new, SignPostMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<ColoredDecoration, BedMarker> BED_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("bed"), BedMarker::new, BedMarker::getFromWorld, ColoredDecoration::new);
    public static final CustomDecorationType<ColoredDecoration, FlagMarker> FLAG_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("flag"), FlagMarker::new, FlagMarker::getFromWorld, ColoredDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, NetherPortalMarker> NETHER_PORTAL_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("nether_portal"), NetherPortalMarker::new, NetherPortalMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, BeaconMarker> BEACON_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("beacon"), BeaconMarker::new, BeaconMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<ColoredDecoration, CeilingBannerMarker> BANNER_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("banner"), CeilingBannerMarker::new, CeilingBannerMarker::getFromWorld, ColoredDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, ChestMarker> CHEST_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("chest"), ChestMarker::new, ChestMarker::getFromWorld, CustomMapDecoration::new);
    public static final CustomDecorationType<CustomMapDecoration, WaystoneMarker> WAYSTONE_DECORATION_TYPE = CustomDecorationType.withWorldMarker(
            Supplementaries.res("waystone"), WaystoneMarker::new, WaystoneMarker::getFromWorld, CustomMapDecoration::new);

    public static final DataObjectReference<MapDecorationType<?,?>> DEATH_MARKER =
    new DataObjectReference<>(Supplementaries.res("death_marker"), MapDecorationRegistry.REGISTRY_KEY);



    public static void init() {

        MapDecorationRegistry.registerCustomType(SIGN_POST_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(BED_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(FLAG_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(NETHER_PORTAL_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(BEACON_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(BANNER_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(CHEST_DECORATION_TYPE);
        MapDecorationRegistry.registerCustomType(WAYSTONE_DECORATION_TYPE);

        MapDecorationRegistry.addDynamicServerMarkersEvent(ModMapMarkers::getForPlayer);
    }

    public static Set<MapBlockMarker<?>> getForPlayer(Player player, int mapId, MapItemSavedData data) {
        var v = player.getLastDeathLocation();
        if (v.isPresent() && data.dimension.equals(v.get().dimension())) {
            if (CommonConfigs.Tweaks.DEATH_MARKER.get().isOn(player)) {
                MapBlockMarker<?> marker = DEATH_MARKER.get().createEmptyMarker();
                marker.setPos(v.get().pos());
                marker.setName(Component.translatable("message.supplementaries.death_marker"));
                return Set.of(marker);
            }
        }
        return Set.of();
    }

}
