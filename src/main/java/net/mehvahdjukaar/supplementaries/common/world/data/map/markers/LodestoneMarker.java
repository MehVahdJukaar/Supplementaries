package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;

public class LodestoneMarker extends MapBlockMarker<CustomMapDecoration> {

    public LodestoneMarker() {
        super(CMDreg.NETHER_PORTAL_DECORATION_TYPE);
    }

    public LodestoneMarker(BlockPos pos) {
        super(CMDreg.NETHER_PORTAL_DECORATION_TYPE, pos);
    }

    @Nullable
    public static LodestoneMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockState(pos).is(Blocks.LODESTONE)) {
            return new LodestoneMarker(pos);
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
