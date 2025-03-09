package net.mehvahdjukaar.supplementaries.mixins;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.common.items.EmptySliceMapItem;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.DepthDataHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.MapLightHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(MapItem.class)
public abstract class MapItemMixin {

    @ModifyExpressionValue(method = "update", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z"))
    public boolean supp$removeCeiling(boolean original, @Share("heightLock") LocalIntRef height) {
        if (original && height.get() != Integer.MAX_VALUE && CommonConfigs.Tools.SLICE_MAP_ENABLED.get()) {
            return false;
        }
        return original;
    }


    @Inject(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z",
            shift = At.Shift.BEFORE,
            ordinal = 0),
            require = 1,
            cancellable = true)
    public void supp$checkHeightLock(Level level, Entity viewer, MapItemSavedData data, CallbackInfo ci,
                                @Local(ordinal = 5) LocalIntRef range,
                                @Share("customColorMap") LocalRef<Map<Vector2i, Pair<BlockPos, Multiset<Block>>>> colorMap,
                                @Share("customLightMap") LocalRef<Map<Vector2i, List<Vector2i>>> lightMap,
                                @Share("heightLock") LocalIntRef height) {
        int mapHeight = DepthDataHandler.getMapHeight(data).orElse(Integer.MAX_VALUE);
        height.set(mapHeight);
        colorMap.set(CommonConfigs.Tweaks.TINTED_MAP.get() ? new HashMap<>() : null);
        lightMap.set(MapLightHandler.isActive() ? new HashMap<>() : null);
        if (mapHeight != Integer.MAX_VALUE) {
            if (!DepthDataHandler.canPlayerSee(mapHeight, viewer)) {
                ci.cancel();
            }
            range.set((int) (range.get() * DepthDataHandler.getRangeMultiplier()));
        }
    }


    @ModifyExpressionValue(method = "update", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
    public int supp$modifySampleHeight(int original, @Share("heightLock") LocalIntRef height) {
        int h = height.get();
        if (h != Integer.MAX_VALUE) return Math.min(original, h);
        return original;
    }


    @WrapOperation(method = "update", at = @At(
            value = "INVOKE",
            ordinal = 3,
            target = "Lnet/minecraft/world/level/block/state/BlockState;getMapColor(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/MapColor;"))
    public MapColor supp$removeXrayAndAddAccurateColor(BlockState instance, BlockGetter level, BlockPos pos, Operation<MapColor> operation,
                                                  @Local(argsOnly = true) Level l,
                                                  @Local LevelChunk chunk,
                                                  @Local(ordinal = 14) int w,
                                                  @Local(ordinal = 0) BlockState state,
                                                  @Local(ordinal = 6) int k1,
                                                  @Local(ordinal = 7) int l1,
                                                  @Share("customColorMap") LocalRef<Map<Vector2i, Pair<BlockPos, Multiset<Block>>>> colorMap,
                                                  @Share("customLightMap") LocalRef<Map<Vector2i, List<Vector2i>>> lightMap,
                                                  @Share("heightLock") LocalIntRef height) {
        MapColor cutoffColor = null;
        if ((height.get() != Integer.MAX_VALUE && height.get() <= w)) {
            cutoffColor = DepthDataHandler.getCutoffColor(pos, chunk);
        }

        if (lightMap.get() != null) {
            //slice maps will consume more data... we need this so pixels offslice render fullbright
            int brightness = (cutoffColor != null && cutoffColor != MapColor.NONE) ? 15 :
                    l.getBrightness(LightLayer.BLOCK, pos.above());
            // no skylight we default to max skylight so we dont save the dat as packed will be 0, default.
            int sky = l.dimensionType().hasSkyLight() ? l.getBrightness(LightLayer.SKY, pos.above()) : 15;
            lightMap.get().computeIfAbsent(new Vector2i(k1, l1), p -> new ArrayList<>())
                    .add(new Vector2i(brightness, sky));
        }
        if (cutoffColor != null) return cutoffColor;

        if (colorMap.get() != null) {
            colorMap.get().computeIfAbsent(new Vector2i(k1, l1), p -> Pair.of(pos, LinkedHashMultiset.create()))
                    .getSecond()
                    .add(state.getBlock());
        }
        return operation.call(instance, level, pos);
    }

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;updateColor(IIB)Z"
    ))
    public boolean supp$updateCustomColor(boolean original,
                                     Level level, Entity viewer, MapItemSavedData data,
                                     @Local(ordinal = 6) int x,
                                     @Local(ordinal = 7) int z,
                                     @Share("customColorMap") LocalRef<Map<Vector2i, Pair<BlockPos, Multiset<Block>>>> colorMap,
                                     @Share("customLightMap") LocalRef<Map<Vector2i, List<Vector2i>>> lightMap) {
        if (colorMap.get() != null) {
            var l = colorMap.get().get(new Vector2i(x, z));
            if (l != null) {
                Block block = Iterables.getFirst(Multisets.copyHighestCountFirst(l.getSecond()), Blocks.AIR);
                ColoredMapHandler.ColorData c = ColoredMapHandler.getColorData(data);
                c.markColored(x, z, block, level, l.getFirst(), data);
            }
        }
        if (lightMap.get() != null) {
            if (lightMap.get() == null) lightMap.set(new HashMap<>());
            var l = lightMap.get().get(new Vector2i(x, z));
            if (l != null) {
                int blockLight = (int) l.stream().mapToDouble(v -> v.x).average().orElse(0);
                int skyLight = (int) l.stream().mapToDouble(v -> v.y).average().orElse(0);
                var c = MapLightHandler.getLightData(data);
                c.setLightLevel(x, z, blockLight, skyLight, data);
            }
        }

        return original;
    }

}