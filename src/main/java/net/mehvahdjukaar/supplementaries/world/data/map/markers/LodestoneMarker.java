package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class LodestoneMarker extends MapWorldMarker<CustomDecoration> {

    public LodestoneMarker() {
        super(CMDreg.LODESTONE_DECORATION_TYPE);
    }

    public LodestoneMarker(BlockPos pos) {
        this();
        this.setPos(pos);
    }

    @Nullable
    public static LodestoneMarker getFromWorld(IBlockReader world, BlockPos pos){
        if(world.getBlockState(pos).is(Blocks.LODESTONE)){
            return new LodestoneMarker(pos);
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
