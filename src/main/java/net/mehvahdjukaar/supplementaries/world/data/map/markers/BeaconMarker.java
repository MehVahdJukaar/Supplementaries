package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.NamedMapWorldMarker;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BeaconMarker extends NamedMapWorldMarker<CustomDecoration> {
    //additional data to be stored

    public BeaconMarker() {
        super(CMDreg.BEACON_DECORATION_TYPE);
    }
    public BeaconMarker(BlockPos pos, @Nullable ITextComponent name) {
        this();
        this.setPos(pos);
        this.name = name;
    }

    @Nullable
    public static BeaconMarker getFromWorld(IBlockReader world, BlockPos pos){
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof BeaconTileEntity) {
            BeaconTileEntity te = ((BeaconTileEntity) tileentity);
            ITextComponent name = te.name;
            return new BeaconMarker(pos,name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,name);
    }
}
