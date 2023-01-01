package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RespawnAnchorBlock;

import javax.annotation.Nullable;

public class RespawnAnchorMarker extends MapBlockMarker<CustomMapDecoration> {

    public RespawnAnchorMarker() {
        super(ModMapMarkers.NETHER_PORTAL_DECORATION_TYPE);
    }

    public RespawnAnchorMarker(BlockPos pos) {
        super(ModMapMarkers.NETHER_PORTAL_DECORATION_TYPE, pos);
    }

    @Nullable
    public static RespawnAnchorMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock) {
            return new RespawnAnchorMarker(pos);
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
