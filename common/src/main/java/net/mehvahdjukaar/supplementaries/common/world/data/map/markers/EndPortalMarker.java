package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EndPortalBlock;

import javax.annotation.Nullable;

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
