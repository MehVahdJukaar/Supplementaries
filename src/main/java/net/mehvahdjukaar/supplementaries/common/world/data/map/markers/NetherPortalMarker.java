package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.NetherPortalBlock;

import javax.annotation.Nullable;

public class NetherPortalMarker extends MapBlockMarker<CustomMapDecoration> {

    public NetherPortalMarker() {
        super(CMDreg.NETHER_PORTAL_DECORATION_TYPE);
    }

    public NetherPortalMarker(BlockPos pos) {
        super(CMDreg.NETHER_PORTAL_DECORATION_TYPE, pos);
    }

    @Nullable
    public static NetherPortalMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof NetherPortalBlock ||
                world.getFluidState(pos).getType().getRegistryName().toString().equals("betterportals:portal_fluid")) {
            return new NetherPortalMarker(pos);
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
