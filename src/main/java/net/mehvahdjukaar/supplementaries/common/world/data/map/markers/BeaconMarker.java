package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.NamedMapWorldMarker;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

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
        if (world.getBlockEntity(pos) instanceof BeaconBlockEntity tile) {
            Component name = tile.name;
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
