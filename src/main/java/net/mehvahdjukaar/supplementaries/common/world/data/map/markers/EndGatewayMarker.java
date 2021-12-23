package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EndGatewayBlock;

import javax.annotation.Nullable;

public class EndGatewayMarker extends MapWorldMarker<CustomDecoration> {

    public EndGatewayMarker() {
        super(CMDreg.END_GATEWAY_DECORATION_TYPE);
    }

    public EndGatewayMarker(BlockPos pos) {
        this();
        this.setPos(pos);
    }

    @Nullable
    public static EndGatewayMarker getFromWorld(BlockGetter world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof EndGatewayBlock){
            return new EndGatewayMarker(pos);
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
