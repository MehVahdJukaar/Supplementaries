package net.mehvahdjukaar.supplementaries.world.data.map.example;

import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapWorldMarker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class CMDExampleMarker extends MapWorldMarker<CustomDecoration> {
    //additional data to be stored
    private final ITextComponent name;
    public CMDExampleMarker(BlockPos pos, @Nullable ITextComponent name) {
        super(CMDExampleReg.EXAMPLE_DECORATION_TYPE, pos);
        this.name = name;
    }

    @Override
    public CompoundNBT saveToNBT() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.getPos()));
        if (this.name != null) {
            compoundnbt.putString("Name", ITextComponent.Serializer.toJson(this.name));
        }
        return compoundnbt;
    }

    //get a marker from nbt
    public static CMDExampleMarker loadFromNBT(CompoundNBT compound){
        BlockPos blockpos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
        ITextComponent itextcomponent = compound.contains("Name") ? ITextComponent.Serializer.fromJson(compound.getString("Name")) : null;
        return new CMDExampleMarker(blockpos, itextcomponent);
    }

    //get a marker from world
    @Nullable
    public static CMDExampleMarker getFromWorld(IBlockReader world, BlockPos pos){
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile sign = ((SignPostBlockTile) tileentity);
            ITextComponent name = sign.getTextHolder().getText(0);
            return new CMDExampleMarker(pos,name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,name);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            CMDExampleMarker marker = (CMDExampleMarker)other;
            return Objects.equals(this.getPos(), marker.getPos())&& Objects.equals(this.name, marker.name);
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.getPos(), this.name);
    }

    @Override
    public boolean shouldUpdate(MapWorldMarker<?> other) {
        if(other instanceof CMDExampleMarker){
            return !Objects.equals(this.name,((CMDExampleMarker) other).name);
        }
        return false;
    }

    @Override
    public CustomDecoration updateDecoration(CustomDecoration old) {
        return new CustomDecoration(old.getType(), old.getX(), old.getY(), old.getRot(), this.name);
    }
}
