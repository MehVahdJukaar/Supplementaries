package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public class RespawnAnchorMarker extends MapWorldMarker<CustomDecoration> {

    public RespawnAnchorMarker() {
        super(CMDreg.RESPAWN_ANCHOR_DECORATION_TYPE);
    }

    public RespawnAnchorMarker(BlockPos pos) {
        this();
        this.setPos(pos);
    }

    @Nullable
    public static RespawnAnchorMarker getFromWorld(BlockGetter world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock){
            return new RespawnAnchorMarker(pos);
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
