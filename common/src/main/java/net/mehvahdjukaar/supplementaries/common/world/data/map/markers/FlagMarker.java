package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.NamedMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.common.world.data.map.ModMapMarkers;
import net.mehvahdjukaar.supplementaries.common.world.data.map.ColoredDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.Objects;

public class FlagMarker extends NamedMapBlockMarker<ColoredDecoration> {

    private DyeColor color;

    public FlagMarker() {
        super(ModMapMarkers.FLAG_DECORATION_TYPE);
    }

    public FlagMarker(BlockPos pos, DyeColor color, @Nullable Component name) {
        super(ModMapMarkers.FLAG_DECORATION_TYPE, pos);
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
            DyeColor dyecolor = tile.getColor();
            Component name = tile.hasCustomName() ? tile.getCustomName() : null;
            return new FlagMarker(pos, dyecolor, name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public ColoredDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new ColoredDecoration(this.getType(), mapX, mapY, rot, this.name, this.color);
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
