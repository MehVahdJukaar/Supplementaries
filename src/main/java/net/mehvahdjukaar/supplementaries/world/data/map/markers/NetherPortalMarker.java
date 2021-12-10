package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.NetherPortalBlock;

import javax.annotation.Nullable;

public class NetherPortalMarker extends MapWorldMarker<CustomDecoration> {

    public NetherPortalMarker() {
        super(CMDreg.NETHER_PORTAL_DECORATION_TYPE);
    }

    public NetherPortalMarker(BlockPos pos) {
        this();
        this.setPos(pos);
    }

    @Nullable
    public static NetherPortalMarker getFromWorld(BlockGetter world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof NetherPortalBlock ||
                world.getFluidState(pos).getType().getRegistryName().toString().equals("betterportals:portal_fluid")) {
            return new NetherPortalMarker(pos);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,null);
    }
}
