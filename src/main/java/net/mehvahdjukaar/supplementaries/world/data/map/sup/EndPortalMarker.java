package net.mehvahdjukaar.supplementaries.world.data.map.sup;

import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapWorldMarker;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class EndPortalMarker extends MapWorldMarker<CustomDecoration> {

    public EndPortalMarker(BlockPos pos, @Nullable ITextComponent name) {
        super(CMDreg.END_PORTAL_DECORATION_TYPE, pos);
    }

    @Override
    public CompoundNBT saveToNBT() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.getPos()));
        return compoundnbt;
    }

    public static EndPortalMarker loadFromNBT(CompoundNBT compound){
        BlockPos blockpos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
        return new EndPortalMarker(blockpos,null);
    }

    @Nullable
    public static EndPortalMarker getFromWorld(IBlockReader world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof EndPortalBlock){
            return new EndPortalMarker(pos,null);
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
            EndPortalMarker marker = (EndPortalMarker)other;
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
