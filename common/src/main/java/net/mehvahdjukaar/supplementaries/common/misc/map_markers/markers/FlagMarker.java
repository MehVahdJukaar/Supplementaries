package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class FlagMarker extends ColoredMarker {

    public FlagMarker() {
        super(ModMapMarkers.FLAG_DECORATION_TYPE);
    }

    public FlagMarker(BlockPos pos, DyeColor color, @Nullable Component name) {
        super(ModMapMarkers.FLAG_DECORATION_TYPE, pos, color, name);
    }

    @Nullable
    public static FlagMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FlagBlockTile tile) {
            DyeColor dyecolor = tile.getColor();
            Component name = tile.hasCustomName() ? tile.getCustomName() : null;
            return new FlagMarker(pos, dyecolor, name);
        } else {
            return null;
        }
    }

}
