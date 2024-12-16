package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class AetherPortalMarker extends SimpleMapBlockMarker {

    public AetherPortalMarker() {
        super(ModMapMarkers.AETHER_PORTAL_DECORATION_TYPE);
    }

    @Nullable
    public static AetherPortalMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (Utils.getID(getType().toString().equals("aether:aether_portal")) {
            var m = new AetherPortalMarker();
            m.setPos(pos);
            return m;
        } else {
            return null;
        }
    }
}
