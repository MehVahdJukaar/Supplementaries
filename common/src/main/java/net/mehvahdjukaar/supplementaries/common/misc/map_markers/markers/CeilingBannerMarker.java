package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.NamedMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ColoredDecoration;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof AbstractBannerBlock && !(block instanceof WallBannerBlock) &&
                !(block instanceof BannerBlock)) {
            DyeColor col = BlocksColorAPI.getColor(block);
            if (col != null) {
                BlockEntity be = world.getBlockEntity(pos);
                Component name = be instanceof Nameable n && n.hasCustomName() ? n.getCustomName() : null;
                return new CeilingBannerMarker(pos, col, name);
            }
        }
        return null;
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
