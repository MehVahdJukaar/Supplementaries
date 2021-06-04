package net.mehvahdjukaar.supplementaries.world.data.map.lib;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

/**
 * utility class do not instance
 */
public class DummyMapWorldMarker extends MapWorldMarker<CustomDecoration> {
    public DummyMapWorldMarker(CustomDecorationType<CustomDecoration,?> type, BlockPos pos) {
        super(type, pos);
    }
    public DummyMapWorldMarker(CustomDecorationType<CustomDecoration,?> type, int x, int z) {
        this(type, new BlockPos(x,64,z));
    }
    @Override
    protected CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,null);
    }
    @Override
    public CompoundNBT saveToNBT(){return new CompoundNBT();}
    @Override
    public boolean equals(Object other) {return false;}
    @Override
    public int hashCode() {return 0;}
}
