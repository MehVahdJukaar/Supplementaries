package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.WaystonesCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.Nullable;

public class WaystoneMarker extends SimpleMapBlockMarker {

    public WaystoneMarker() {
        super(ModMapMarkers.WAYSTONE_DECORATION_TYPE);
    }

    public WaystoneMarker(BlockPos pos, @Nullable Component name) {
        super(ModMapMarkers.WAYSTONE_DECORATION_TYPE);
        this.setName(name);
        this.setPos(pos);
    }

    @Nullable
    public static WaystoneMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (CompatHandler.WAYSTONES) {
            var te = world.getBlockEntity(pos);

            if (WaystonesCompat.isWaystone(te)) {
                Component name = WaystonesCompat.getName(te);
                return new WaystoneMarker(pos, name);
             }
        }
        return null;
    }

}
