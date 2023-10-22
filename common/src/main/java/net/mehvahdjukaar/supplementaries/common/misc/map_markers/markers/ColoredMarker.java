package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ColoredDecoration;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class ColoredMarker extends MapBlockMarker<ColoredDecoration> {

    private DyeColor color = DyeColor.WHITE;

    protected ColoredMarker(MapDecorationType<ColoredDecoration,?> type) {
        super(type);
    }

    protected ColoredMarker(MapDecorationType<?,?> type, BlockPos pos, DyeColor color, @Nullable Component name) {
        super(ModMapMarkers.FLAG_DECORATION_TYPE);
        this.color = color;
        this.setName(name);
        this.setPos(pos);
    }

    @Override
    public CompoundTag saveToNBT(CompoundTag compound) {
        super.saveToNBT(compound);
        compound.putString("Color", this.color.getName());
        return compound;
    }

    @Override
    public void loadFromNBT(CompoundTag compound) {
        super.loadFromNBT(compound);
        this.color = DyeColor.byName(compound.getString("Color"), DyeColor.WHITE);
    }

    @Nullable
    @Override
    public ColoredDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new ColoredDecoration(this.getType(), mapX, mapY, rot, this.getName(), this.color);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            ColoredMarker marker = (ColoredMarker) other;
            return Objects.equals(this.getPos(), marker.getPos()) && this.color == marker.color && Objects.equals(marker.getName(), this.getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPos(),this.getName(),  this.color);
    }

}
