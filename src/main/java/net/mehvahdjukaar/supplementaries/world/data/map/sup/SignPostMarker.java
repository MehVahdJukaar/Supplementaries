package net.mehvahdjukaar.supplementaries.world.data.map.sup;

import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapWorldMarker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class SignPostMarker extends MapWorldMarker<CustomDecoration> {
    //additional data to be stored
    @Nullable
    private final ITextComponent name;
    public SignPostMarker(BlockPos pos, @Nullable ITextComponent name) {
        super(CMDreg.SIGN_POST_DECORATION_TYPE, pos);
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

    public static SignPostMarker loadFromNBT(CompoundNBT compound){
        BlockPos blockpos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
        ITextComponent itextcomponent = compound.contains("Name") ? ITextComponent.Serializer.fromJson(compound.getString("Name")) : null;
        return new SignPostMarker(blockpos,itextcomponent);
    }

    @Nullable
    public static SignPostMarker getFromWorld(IBlockReader world, BlockPos pos){
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile te = ((SignPostBlockTile) tileentity);
            ITextComponent t = new StringTextComponent("");
            if(te.up)t=te.textHolder.signText[0];
            if(te.down && t.getString().isEmpty())
                t=te.textHolder.signText[1];
            if(t.getString().isEmpty())t=null;
            return new SignPostMarker(pos,t);
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
            SignPostMarker marker = (SignPostMarker)other;
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
        if(other instanceof SignPostMarker){
            return !Objects.equals(this.name,((SignPostMarker) other).name);
        }
        return false;
    }

    @Override
    public CustomDecoration updateDecoration(CustomDecoration old) {
        return new CustomDecoration(old.getType(), old.getX(), old.getY(), old.getRot(), this.name);
    }
}
