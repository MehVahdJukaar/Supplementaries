package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLSpecialMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.decoration.SimpleMapMarker;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.WaystonesCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class ModMapMarkers {


    //builtin code defined ones

    //with markers
    public static final DynamicHolder<MLMapDecorationType<?, ?>> DEATH_MARKER =
            DynamicHolder.of(Supplementaries.res("death_marker"), MapDataRegistry.REGISTRY_KEY);

    public static final ResourceLocation WAY_SIGN_FACTORY_ID = Supplementaries.res("way_sign");
    public static final ResourceLocation WAYSTONE_FACTORY_ID = Supplementaries.res("waystone");
    public static final ResourceLocation BANNER_FACTORY_ID = Supplementaries.res("banner");
    public static final ResourceLocation BED_FACTORY_ID = Supplementaries.res("bed");
    public static final ResourceLocation FLAG_FACTORY_ID = Supplementaries.res("flag");

    public static void init() {
        MapDataRegistry.registerSpecialMapDecorationTypeFactory(WAY_SIGN_FACTORY_ID, () ->
                MLSpecialMapDecorationType.fromWorldSimple(ModMapMarkers::signPost));
        MapDataRegistry.registerSpecialMapDecorationTypeFactory(WAYSTONE_FACTORY_ID, () ->
                MLSpecialMapDecorationType.fromWorldSimple(ModMapMarkers::waystone));

        MapDataRegistry.registerSpecialMapDecorationTypeFactory(BANNER_FACTORY_ID, () ->
                MLSpecialMapDecorationType.fromWorldCustomMarker(
                        ColoredMarker.DIRECT_CODEC, ColoredDecoration.DIRECT_CODEC,
                        ModMapMarkers::banner));
        MapDataRegistry.registerSpecialMapDecorationTypeFactory(BED_FACTORY_ID, () ->
                MLSpecialMapDecorationType.fromWorldCustomMarker(
                        ColoredMarker.DIRECT_CODEC, ColoredDecoration.DIRECT_CODEC,
                        ModMapMarkers::bed));
        MapDataRegistry.registerSpecialMapDecorationTypeFactory(FLAG_FACTORY_ID, () ->
                MLSpecialMapDecorationType.fromWorldCustomMarker(
                        ColoredMarker.DIRECT_CODEC, ColoredDecoration.DIRECT_CODEC,
                        ModMapMarkers::flag));

        MapDataRegistry.addDynamicServerMarkersEvent(ModMapMarkers::getForPlayer);
    }


    public static Set<MLMapMarker<?>> getForPlayer(Player player, MapId mapId, MapItemSavedData data) {
        var v = player.getLastDeathLocation();
        if (v.isPresent() && data.dimension.equals(v.get().dimension())) {
            if (CommonConfigs.Tweaks.DEATH_MARKER.get().isOn(player)) {
                MLMapMarker<?> marker = new SimpleMapMarker(DEATH_MARKER, v.get().pos(), 0f,
                        Optional.of(Component.translatable("message.supplementaries.death_marker")));
                return Set.of(marker);
            }
        }
        return Set.of();
    }


    @Nullable
    private static SimpleMapMarker signPost(Holder<MLMapDecorationType<?, ?>> type,
                                            BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            Component t = Component.literal("");
            if (tile.getSignUp().active()) t = tile.getTextHolder(0).getMessage(0, false);
            if (tile.getSignDown().active() && t.getString().isEmpty())
                t = tile.getTextHolder(1).getMessage(0, false);
            if (t.getString().isEmpty()) t = null;
            return new SimpleMapMarker(type, pos, 0f, Optional.ofNullable(t));
        } else {
            return null;
        }
    }

    @Nullable
    private static ColoredMarker bed(Holder<MLMapDecorationType<?, ?>> type,
                                     BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BedBlockEntity tile) {
            DyeColor dyecolor = tile.getColor();
            return new ColoredMarker(type, pos, dyecolor);
        } else {
            return null;
        }
    }

    @Nullable
    private static ColoredMarker flag(Holder<MLMapDecorationType<?, ?>> type,
                                      BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FlagBlockTile tile) {
            DyeColor dyecolor = tile.getColor();
            Component name = tile.hasCustomName() ? tile.getCustomName() : null;
            return new ColoredMarker(type, pos, name, dyecolor);
        } else {
            return null;
        }
    }

    @Nullable
    private static ColoredMarker banner(Holder<MLMapDecorationType<?, ?>> type,
                                        BlockGetter world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        //for amendments
        if (block instanceof AbstractBannerBlock && !(block instanceof WallBannerBlock) &&
                !(block instanceof BannerBlock)) {
            DyeColor col = BlocksColorAPI.getColor(block);
            if (col != null) {
                BlockEntity be = world.getBlockEntity(pos);
                Component name = be instanceof Nameable n && n.hasCustomName() ? n.getCustomName() : null;
                return new ColoredMarker(type, pos, name, col);
            }
        }
        return null;
    }

    @Nullable
    private static SimpleMapMarker waystone(Holder<MLMapDecorationType<?, ?>> type,
                                            BlockGetter world, BlockPos pos) {
        if (CompatHandler.WAYSTONES) {
            var te = world.getBlockEntity(pos);

            if (WaystonesCompat.isWaystone(te)) {
                Component name = WaystonesCompat.getName(te);
                return new SimpleMapMarker(type, pos, 0f, Optional.of(name));
            }
        }
        return null;
    }
}
