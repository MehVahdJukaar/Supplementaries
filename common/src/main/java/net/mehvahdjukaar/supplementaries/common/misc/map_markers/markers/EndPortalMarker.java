package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EndPortalBlock;

import org.jetbrains.annotations.Nullable;

public class EndPortalMarker extends MapBlockMarker<CustomMapDecoration> {

    public EndPortalMarker() {
        super(ModMapMarkers.END_PORTAL_DECORATION_TYPE);
    }

    public EndPortalMarker(BlockPos pos) {
        super(ModMapMarkers.END_PORTAL_DECORATION_TYPE, pos);
    }

    @Nullable
    public static EndPortalMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof EndPortalBlock) {
            return new EndPortalMarker(pos);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomMapDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomMapDecoration(this.getType(), mapX, mapY, rot, null);
    }
}
