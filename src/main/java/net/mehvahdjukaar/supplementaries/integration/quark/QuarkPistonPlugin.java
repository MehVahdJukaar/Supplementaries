package net.mehvahdjukaar.supplementaries.integration.quark;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

public class QuarkPistonPlugin {

    private static Field MOVEMENTS = null;

    //not really a plugin
    @Nullable
    public static BlockEntity getMovingTile(BlockPos pos, Level world) {
        return PistonsMoveTileEntitiesModule.getMovement(world, pos);
    }

    public static boolean updateMovingTIle(BlockPos pos, Level world, BlockEntity tile) {
        //not very nice of me to change its private fields :/
        try {
            //Class c = Class.forName("vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule");
            if (MOVEMENTS == null) {
                MOVEMENTS = ObfuscationReflectionHelper.findField(PistonsMoveTileEntitiesModule.class, "movements");
            }

            Object o = MOVEMENTS.get(null);
            if (o instanceof WeakHashMap) {
                WeakHashMap<Level, Map<BlockPos, CompoundTag>> movements = (WeakHashMap<Level, Map<BlockPos, CompoundTag>>) o;
                if (movements.containsKey(world)) {
                    Map<BlockPos, CompoundTag> worldMovements = movements.get(world);
                    if (worldMovements.containsKey(pos)) {
                        worldMovements.remove(pos);
                        worldMovements.put(pos, tile.saveWithFullMetadata());
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean canMoveTile(BlockState state) {
        return !PistonsMoveTileEntitiesModule.shouldMoveTE(true, state);
    }
}
