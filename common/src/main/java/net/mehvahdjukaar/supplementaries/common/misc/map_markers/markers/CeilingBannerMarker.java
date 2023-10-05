package net.mehvahdjukaar.supplementaries.common.misc.map_markers.markers;

import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
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

public class CeilingBannerMarker extends ColoredMarker {

    public CeilingBannerMarker() {
        super(ModMapMarkers.BANNER_DECORATION_TYPE);
    }

    public CeilingBannerMarker(BlockPos pos, DyeColor color, @Nullable Component name) {
        super(ModMapMarkers.BANNER_DECORATION_TYPE, pos, color, name);
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
}
