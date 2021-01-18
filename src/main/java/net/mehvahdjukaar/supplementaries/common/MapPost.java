package net.mehvahdjukaar.supplementaries.common;


import net.mehvahdjukaar.supplementaries.blocks.tiles.SignPostBlockTile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class MapPost {
    public final BlockPos pos;
    @Nullable
    public final ITextComponent name;

    public MapPost(BlockPos pos, @Nullable ITextComponent name) {
        this.pos = pos;
        this.name = name;
    }

    public static MapPost read(CompoundNBT nbt) {
        BlockPos blockpos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
        ITextComponent itextcomponent = nbt.contains("Name") ? ITextComponent.Serializer.getComponentFromJson(nbt.getString("Name")) : null;
        return new MapPost(blockpos, itextcomponent);
    }

    @Nullable
    public static MapPost fromWorld(IBlockReader reader, BlockPos pos) {
        TileEntity tileentity = reader.getTileEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile te = (SignPostBlockTile)tileentity;

            ITextComponent t = new StringTextComponent("");
            if(te.up)t=te.textHolder.signText[0];
            if(te.down && t.getString().isEmpty())
                t=te.textHolder.signText[1];
            if(t.getString().isEmpty())t=null;

            return new MapPost(pos, t);
        } else {
            return null;
        }
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            MapPost mapbanner = (MapPost)p_equals_1_;
            return Objects.equals(this.pos, mapbanner.pos) && Objects.equals(this.name, mapbanner.name);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(this.pos, this.name);
    }

    public CompoundNBT write() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.pos));
        if (this.name != null) {
            compoundnbt.putString("Name", ITextComponent.Serializer.toJson(this.name));
        }
        return compoundnbt;
    }

    public String getId() {
        return "post-" + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ();
    }
}