package net.mehvahdjukaar.supplementaries.world.data.map.sup;

import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapWorldMarker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class EndCrystalMarker extends MapWorldMarker<CustomDecoration> {
    private final int entityId;
    public EndCrystalMarker(BlockPos pos, @Nullable ITextComponent name, int entityId) {
        super(CMDreg.END_GATEWAY_DECORATION_TYPE, pos);
        this.entityId = entityId;
    }

    @Override
    public CompoundNBT saveToNBT() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.getPos()));
        compoundnbt.putInt("EntityId", this.entityId);
        return compoundnbt;
    }

    public static EndCrystalMarker loadFromNBT(CompoundNBT compound){
        BlockPos blockpos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
        int id = compound.getInt("EntityId");
        return new EndCrystalMarker(blockpos,null,id);
    }

    @Nullable
    public static EndCrystalMarker getFromWorld(IBlockReader world, BlockPos pos){
        if(false){
            return new EndCrystalMarker(pos,null,0);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,null);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            EndCrystalMarker marker = (EndCrystalMarker)other;
            return Objects.equals(this.getPos(), marker.getPos());
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.getPos());
    }

}
