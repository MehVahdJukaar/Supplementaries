package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.mehvahdjukaar.supplementaries.common.items.SliceMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItem.class)
public class MapItemMixin {

    @ModifyExpressionValue(method = "update", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z"))
    public boolean removeCeiling(boolean original,  @Share("heightLock") LocalIntRef height) {
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
                                @Share("heightLock") LocalIntRef height) {
        int mapHeight = SliceMap.getMapHeight(data);
        height.set(mapHeight);

        if (mapHeight != Integer.MAX_VALUE) {
            if (!SliceMap.canPlayerSee(mapHeight, viewer)) {
                ci.cancel();
            }
            range.set((int) (range.get() * SliceMap.getRangeMultiplier()));
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
    public MapColor removeXray(BlockState instance, BlockGetter level, BlockPos pos, Operation<MapColor> operation,
                               @Local LevelChunk chunk,
                               @Local(ordinal = 14) int w, @Share("heightLock") LocalIntRef height) {
        if (height.get() != Integer.MAX_VALUE && height.get() <= w) {
            return SliceMap.getCutoffColor(pos, chunk);
        }
        return operation.call(instance, level, pos);
    }


}