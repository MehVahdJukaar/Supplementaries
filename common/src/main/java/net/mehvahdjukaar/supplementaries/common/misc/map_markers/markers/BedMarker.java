package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ColoredDecoration;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import org.jetbrains.annotations.Nullable;

public class BedMarker extends SimpleMapBlockMarker {

    public BedMarker() {
        super(ModMapMarkers.BED_DECORATION_TYPE);
    }

    @Nullable
    public static BedMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof BedBlockEntity tile) {
            DyeColor dyecolor = tile.getColor();
            var b = new BedMarker( );
            b.setPos(pos);
            return b;
        } else {
            return null;
        }
    }

}
