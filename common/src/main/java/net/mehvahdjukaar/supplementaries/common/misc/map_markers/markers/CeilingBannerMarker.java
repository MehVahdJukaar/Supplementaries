package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.NamedMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CeilingBannerBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ColoredDecoration;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class CeilingBannerMarker extends NamedMapBlockMarker<ColoredDecoration> {

    private DyeColor color;

    public CeilingBannerMarker() {
        super(ModMapMarkers.BANNER_DECORATION_TYPE);
    }

    public CeilingBannerMarker(BlockPos pos, DyeColor color, @Nullable Component name) {
        super(ModMapMarkers.BANNER_DECORATION_TYPE, pos);
        this.color = color;
        this.name = name;
    }

    @Override
    public CompoundTag saveToNBT(CompoundTag tag) {
        super.saveToNBT(tag);
        tag.putString("Color", this.color.getName());
        return tag;
    }

    @Override
    public void loadFromNBT(CompoundTag compound) {
        super.loadFromNBT(compound);
        this.color = DyeColor.byName(compound.getString("Color"), DyeColor.WHITE);
    }

    @Nullable
    public static CeilingBannerMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CeilingBannerBlockTile tile) {
            DyeColor dyecolor = tile.getBaseColor(tile::getBlockState);
            Component name = tile.hasCustomName() ? tile.getCustomName() : null;
            return new CeilingBannerMarker(pos, dyecolor, name);
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
            CeilingBannerMarker marker = (CeilingBannerMarker) other;
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
