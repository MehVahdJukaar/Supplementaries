package net.mehvahdjukaar.supplementaries.world.data.map.lib;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class MapWorldMarker<D extends CustomDecoration>  {
    private final CustomDecorationType<D,?> type;
    private final BlockPos pos;
    //private final int rotation;

    public MapWorldMarker(CustomDecorationType<D,?> type, BlockPos pos) {
        this.type = type;
        this.pos = pos;
        //this.rotation = rotation;
    }

    public CustomDecorationType<D,?> getType() {
        return type;
    }

    public String getTypeId(){
        return this.type.getSerializeId();
    }

    private String getPosId(){
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public String getMarkerId(){
        return this.getTypeId()+"-"+getPosId();
    }

    public abstract CompoundNBT saveToNBT();

    public BlockPos getPos() {
        return this.pos;
    }

    public int getRotation() {
        return 0;
    }

    @Nullable
    protected abstract D doCreateDecoration(byte mapX, byte mapY, byte rot);

    @Nullable
    public D createDecorationFromMarker(byte scale, int x, int z, RegistryKey<World> dimension, boolean locked){
        double worldX = this.getPos().getX();
        double worldZ = this.getPos().getZ();
        double rotation = this.getRotation();

        int i = 1 << scale;
        float f = (float)(worldX - (double)x) / (float)i;
        float f1 = (float)(worldZ - (double)z) / (float)i;
        byte mapX = (byte)((int)((double)(f * 2.0F) + 0.5D));
        byte mapY = (byte)((int)((double)(f1 * 2.0F) + 0.5D));
        byte rot;
        if (f >= -63.0F && f1 >= -63.0F && f <= 63.0F && f1 <= 63.0F) {
            rotation = rotation + (rotation < 0.0D ? -8.0D : 8.0D);
            rot = (byte)((int)(rotation * 16.0D / 360.0D));
            return doCreateDecoration(mapX, mapY, rot);
        }
        return null;
    }

    public abstract boolean equals(Object other);

    public abstract int hashCode();

    /**
     * used to check if a world marker has changed and needs to update its decoration
     * as an example it can be used when a tile entity changes its name and its decoration needs to reflect that
     * @param other marker that needs to be compared wit this
     * @return true if corresponding decoration has to be updated
     */
    public boolean shouldUpdate(MapWorldMarker<?> other){
        return false;
    }

    /**
     * updates my map decoration after should update returns true
     */
    public CustomDecoration updateDecoration(CustomDecoration old){return old;};

}
