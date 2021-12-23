package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EndPortalBlock;

import javax.annotation.Nullable;

public class EndPortalMarker extends MapWorldMarker<CustomDecoration> {

    public EndPortalMarker() {
        super(CMDreg.END_PORTAL_DECORATION_TYPE);
    }

    public EndPortalMarker(BlockPos pos) {
        this();
        this.setPos(pos);
    }

    @Nullable
    public static EndPortalMarker getFromWorld(BlockGetter world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof EndPortalBlock){
            return new EndPortalMarker(pos);
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
