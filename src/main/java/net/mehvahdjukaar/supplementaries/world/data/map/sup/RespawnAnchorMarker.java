package net.mehvahdjukaar.supplementaries.world.data.map.sup;

import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapWorldMarker;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class RespawnAnchorMarker extends MapWorldMarker<CustomDecoration> {

    public RespawnAnchorMarker(BlockPos pos, @Nullable ITextComponent name) {
        super(CMDreg.RESPAWN_ANCHOR_DECORATION_TYPE, pos);
    }

    @Override
    public CompoundNBT saveToNBT() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.getPos()));
        return compoundnbt;
    }

    public static RespawnAnchorMarker loadFromNBT(CompoundNBT compound){
        BlockPos blockpos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
        return new RespawnAnchorMarker(blockpos,null);
    }

    @Nullable
    public static RespawnAnchorMarker getFromWorld(IBlockReader world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof RespawnAnchorBlock){
            return new RespawnAnchorMarker(pos,null);
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
            RespawnAnchorMarker marker = (RespawnAnchorMarker)other;
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
