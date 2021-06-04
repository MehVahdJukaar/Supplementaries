package net.mehvahdjukaar.supplementaries.world.data.map.sup;

import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapWorldMarker;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class EndGatewayMarker extends MapWorldMarker<CustomDecoration> {

    public EndGatewayMarker(BlockPos pos, @Nullable ITextComponent name) {
        super(CMDreg.END_GATEWAY_DECORATION_TYPE, pos);
    }

    @Override
    public CompoundNBT saveToNBT() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.getPos()));
        return compoundnbt;
    }

    public static EndGatewayMarker loadFromNBT(CompoundNBT compound){
        BlockPos blockpos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
        return new EndGatewayMarker(blockpos,null);
    }

    @Nullable
    public static EndGatewayMarker getFromWorld(IBlockReader world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof EndGatewayBlock){
            return new EndGatewayMarker(pos,null);
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
            EndGatewayMarker marker = (EndGatewayMarker)other;
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
