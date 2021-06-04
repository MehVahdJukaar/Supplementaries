package net.mehvahdjukaar.supplementaries.world.data.map.lib;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Map;

public interface CustomDecorationHolder {
    Map<String, CustomDecoration> getCustomDecorations();
    Map<String, MapWorldMarker<?>> getCustomMarkers();

    void toggleCustomDecoration(IWorld world, BlockPos pos);

    void resetCustomDecoration();
}
