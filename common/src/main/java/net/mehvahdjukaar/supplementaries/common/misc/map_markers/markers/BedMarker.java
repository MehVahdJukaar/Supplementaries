package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import org.jetbrains.annotations.Nullable;

public class BedMarker extends ColoredMarker {

    public BedMarker() {
        super(ModMapMarkers.BED_DECORATION_TYPE);
    }

    public BedMarker(BlockPos pos, DyeColor color) {
        super(ModMapMarkers.BED_DECORATION_TYPE, pos, color, null);
    }

    @Nullable
    public static BedMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof BedBlockEntity tile) {
            DyeColor dyecolor = tile.getColor();
            return new BedMarker(pos, dyecolor);
        } else {
            return null;
        }
    }

}
