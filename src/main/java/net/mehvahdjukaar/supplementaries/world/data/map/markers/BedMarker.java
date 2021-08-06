package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.minecraft.block.BedBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class BedMarker extends MapWorldMarker<CustomDecoration> {
    //additional data to be stored
    private DyeColor color;
    public BedMarker() {
        super(CMDreg.BED_DECORATION_TYPE);
    }
    public BedMarker(BlockPos pos, DyeColor color) {
        this();
        this.setPos(pos);
        this.color = color;
    }

    @Override
    public CompoundNBT saveToNBT(CompoundNBT compoundnbt) {
        super.saveToNBT(compoundnbt);
        compoundnbt.putString("Color", this.color.getName());
        return compoundnbt;
    }

    public void loadFromNBT(CompoundNBT compound){
        super.loadFromNBT(compound);
        this.color = DyeColor.byName(compound.getString("Color"), DyeColor.WHITE);
    }

    @Nullable
    public static BedMarker getFromWorld(IBlockReader world, BlockPos pos){
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof BedTileEntity) {
            DyeColor dyecolor = ((BedBlock)tileentity.getBlockState().getBlock()).getColor();
            return new BedMarker(pos,dyecolor);
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
            BedMarker marker = (BedMarker)other;
            return Objects.equals(this.getPos(), marker.getPos()) && this.color == marker.color;
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.getPos(), this.color);
    }

}
