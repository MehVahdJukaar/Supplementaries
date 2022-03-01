package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;


import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ConduitBlock;

import javax.annotation.Nullable;

public class ConduitMarker extends MapBlockMarker<CustomDecoration> {

    public ConduitMarker() {
        super(CMDreg.CONDUIT_DECORATION_TYPE);
    }

    public ConduitMarker(BlockPos pos) {
        this();
        this.setPos(pos);
    }

    @Nullable
    public static ConduitMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof ConduitBlock) {
            return new ConduitMarker(pos);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(), mapX, mapY, rot, null);
    }

}
