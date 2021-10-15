package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.NamedMapWorldMarker;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public class BeaconMarker extends NamedMapWorldMarker<CustomDecoration> {
    //additional data to be stored

    public BeaconMarker() {
        super(CMDreg.BEACON_DECORATION_TYPE);
    }
    public BeaconMarker(BlockPos pos, @Nullable Component name) {
        this();
        this.setPos(pos);
        this.name = name;
    }

    @Nullable
    public static BeaconMarker getFromWorld(BlockGetter world, BlockPos pos){
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof BeaconBlockEntity) {
            BeaconBlockEntity te = ((BeaconBlockEntity) tileentity);
            Component name = te.name;
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
