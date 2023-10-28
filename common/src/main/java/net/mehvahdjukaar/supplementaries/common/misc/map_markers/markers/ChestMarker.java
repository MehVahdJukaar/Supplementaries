package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ChestMarker extends SimpleMapBlockMarker {

    public ChestMarker() {
        super(ModMapMarkers.CHEST_DECORATION_TYPE);
    }

    public ChestMarker(BlockPos pos, Component name) {
        super(ModMapMarkers.CHEST_DECORATION_TYPE);
        this.setPos(pos);
        this.setName(name);
    }

    @Nullable
    public static ChestMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ChestBlockEntity ce) {
            return new ChestMarker(pos, ce.getCustomName());
        } else {
            return null;
        }
    }

}
