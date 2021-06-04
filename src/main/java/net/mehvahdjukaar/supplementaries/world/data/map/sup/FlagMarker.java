package net.mehvahdjukaar.supplementaries.world.data.map.sup;

import net.mehvahdjukaar.supplementaries.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapWorldMarker;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class FlagMarker extends MapWorldMarker<FlagDecoration> {
    @Nullable
    private final ITextComponent name;
    private final DyeColor color;
    public FlagMarker(BlockPos pos, DyeColor color, @Nullable ITextComponent name) {
        super(CMDreg.FLAG_DECORATION_TYPE, pos);
        this.color = color;
        this.name = name;
    }

    @Override
    public CompoundNBT saveToNBT() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.getPos()));
        compoundnbt.putString("Color", this.color.getName());
        if (this.name != null) {
            compoundnbt.putString("Name", ITextComponent.Serializer.toJson(this.name));
        }
        return compoundnbt;
    }

    public static FlagMarker loadFromNBT(CompoundNBT compound){
        BlockPos blockpos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
        DyeColor dyecolor = DyeColor.byName(compound.getString("Color"), DyeColor.WHITE);
        ITextComponent name = compound.contains("Name") ? ITextComponent.Serializer.fromJson(compound.getString("Name")) : null;
        return new FlagMarker(blockpos,dyecolor,name);
    }

    @Nullable
    public static FlagMarker getFromWorld(IBlockReader world, BlockPos pos){
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof FlagBlockTile) {
            FlagBlockTile flag = ((FlagBlockTile) tileentity);
            DyeColor dyecolor = flag.getBaseColor(flag::getBlockState);
            ITextComponent name = flag.hasCustomName() ? flag.getCustomName() : null;
            return new FlagMarker(pos,dyecolor,name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public FlagDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new FlagDecoration(this.getType(),mapX,mapY,rot,this.name,this.color);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            FlagMarker marker = (FlagMarker)other;
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
