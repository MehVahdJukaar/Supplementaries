package net.mehvahdjukaar.supplementaries.world.data.map.markers;

import net.mehvahdjukaar.selene.map.markers.NamedMapWorldMarker;
import net.mehvahdjukaar.supplementaries.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.world.data.map.FlagDecoration;
import net.minecraft.world.item.DyeColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.Objects;

public class FlagMarker extends NamedMapWorldMarker<FlagDecoration> {

    private DyeColor color;

    public FlagMarker() {
        super(CMDreg.FLAG_DECORATION_TYPE);
    }

    public FlagMarker(BlockPos pos, DyeColor color, @Nullable Component name) {
        this();
        this.pos = pos;
        this.color = color;
        this.name = name;
    }

    @Override
    public CompoundTag saveToNBT(CompoundTag compoundnbt) {
        super.saveToNBT(compoundnbt);
        compoundnbt.putString("Color", this.color.getName());
        return compoundnbt;
    }

    public void loadFromNBT(CompoundTag compound) {
        super.loadFromNBT(compound);
        this.color = DyeColor.byName(compound.getString("Color"), DyeColor.WHITE);
    }

    @Nullable
    public static FlagMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FlagBlockTile tile) {
            DyeColor dyecolor = tile.getBaseColor(tile::getBlockState);
            Component name = tile.hasCustomName() ? tile.getCustomName() : null;
            return new FlagMarker(pos, dyecolor, name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public FlagDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new FlagDecoration(this.getType(), mapX, mapY, rot, this.name, this.color);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            FlagMarker marker = (FlagMarker) other;
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
