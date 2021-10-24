package net.mehvahdjukaar.supplementaries.compat.quark;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class QuarkPistonPlugin {
    //not really a plugin
    @Nullable
    public static BlockEntity getMovingTile(BlockPos pos, Level world) {
        //return PistonsMoveTileEntitiesModule.getMovement(world,pos);
        return null;
    }

    public static boolean updateMovingTIle(BlockPos pos, Level world, BlockEntity tile) {
//        //not very nice of me to change its private fields :/
//        try {
//            //Class c = Class.forName("vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule");
//            Class c = PistonsMoveTileEntitiesModule.class;
//            Field f = ObfuscationReflectionHelper.findField(c,"movements");
//            Object o = f.get(null);
//            if(o instanceof WeakHashMap){
//                WeakHashMap<World, Map<BlockPos, CompoundNBT>> movements = (WeakHashMap<World, Map<BlockPos, CompoundNBT>>) o;
//                if (movements.containsKey(world)) {
//                    Map<BlockPos, CompoundNBT> worldMovements = movements.get(world);
//                    if (worldMovements.containsKey(pos)) {
//                        worldMovements.remove(pos);
//                        worldMovements.put(pos,tile.serializeNBT());
//                        return true;
//                    }
//                }
//            }
//        }
//        catch (Exception ignored) {}
        return false;
    }

    public static boolean canMoveTile(BlockState state) {
        return true;
        //return !PistonsMoveTileEntitiesModule.shouldMoveTE(true, state);
    }
}
