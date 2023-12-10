package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.supplementaries.common.block.tiles.CeilingBannerBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class CeilingBannerMarker extends ColoredMarker {

    public CeilingBannerMarker() {
        super(ModMapMarkers.BANNER_DECORATION_TYPE);
    }

    public CeilingBannerMarker(BlockPos pos, DyeColor color, @Nullable Component name) {
        super(ModMapMarkers.BANNER_DECORATION_TYPE, pos, color, name);
    }

    @Nullable
    public static CeilingBannerMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CeilingBannerBlockTile tile) {
            DyeColor dyecolor = tile.getBaseColor(tile::getBlockState);
            Component name = tile.hasCustomName() ? tile.getCustomName() : null;
            return new CeilingBannerMarker(pos, dyecolor, name);
        } else {
            return null;
        }
    }
}
