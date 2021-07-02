package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

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
    public static NetherPortalMarker getFromWorld(IBlockReader world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof NetherPortalBlock){
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
