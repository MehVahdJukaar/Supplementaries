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
import net.mehvahdjukaar.supplementaries.common.items.SliceMapItem;
import net.mehvahdjukaar.supplementaries.common.misc.ColoredMapHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

import java.util.HashMap;
import java.util.Map;

@Mixin(MapItem.class)
public class MapItemMixin {

    @ModifyExpressionValue(method = "update", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z"))
    public boolean removeCeiling(boolean original, @Share("heightLock") LocalIntRef height) {
        if (original && height.get() != Integer.MAX_VALUE) {
            return false;
        }
        return original;
    }

    //TODO: replace with onUpdatecall override
    @Inject(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z",
            shift = At.Shift.BEFORE,
            ordinal = 0),
            require = 1,
            cancellable = true)
    public void checkHeightLock(Level level, Entity viewer, MapItemSavedData data, CallbackInfo ci,
                                @Local(ordinal = 5) LocalIntRef range,
                                @Share("customColorMap") LocalRef<Map<Vector2i, Multiset<Block>>> colorMap,
                                @Share("heightLock") LocalIntRef height) {
        int mapHeight = SliceMapItem.getMapHeight(data);
        height.set(mapHeight);
        colorMap.set(new HashMap<>());
        if (mapHeight != Integer.MAX_VALUE) {
            if (!SliceMapItem.canPlayerSee(mapHeight, viewer)) {
                ci.cancel();
            }
            range.set((int) (range.get() * SliceMapItem.getRangeMultiplier()));
        }
    }

    @ModifyExpressionValue(method = "update", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
    public int modifySampleHeight(int original, @Share("heightLock") LocalIntRef height) {
        int h = height.get();
        if (h != Integer.MAX_VALUE) return Math.min(original, h);
        return original;
    }

    @WrapOperation(method = "update", at = @At(
            value = "INVOKE",
            ordinal = 3,
            target = "Lnet/minecraft/world/level/block/state/BlockState;getMapColor(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/MapColor;"))
    public MapColor removeXrayAndAddAccurateColor(BlockState instance, BlockGetter level, BlockPos pos, Operation<MapColor> operation,
                                                  @Local LevelChunk chunk,
                                                  @Local(ordinal = 14) int w,
                                                  @Local BlockState state,
                                                  @Local(ordinal = 6) int k1,
                                                  @Local(ordinal = 7) int l1,
                                                  @Share("customColorMap") LocalRef<Map<Vector2i, Multiset<Block>>> colorMap,
                                                  @Share("heightLock") LocalIntRef height) {
        if (height.get() != Integer.MAX_VALUE && height.get() <= w) {
            return SliceMapItem.getCutoffColor(pos, chunk);
        }
        colorMap.get().computeIfAbsent(new Vector2i(k1, l1), p -> LinkedHashMultiset.create())
                .add(state.getBlock());

        return operation.call(instance, level, pos);
    }

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;updateColor(IIB)Z"
    ))
    public boolean updateCustomColor(boolean original, int x, int z, byte color,
                                     @Local MapItemSavedData data,
                                     @Share("customColorMap") LocalRef<Map<Vector2i, Multiset<Block>>> colorMap) {
        var l = colorMap.get().get(new Vector2i(x, z));
        if (l != null) {
            Block block = Iterables.getFirst(Multisets.copyHighestCountFirst(l), Blocks.AIR);
            var c = ColoredMapHandler.getColorData(data);
            if(c != null) c.markColored(x, z, block);
        }

        return original;
    }


}