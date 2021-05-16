package net.mehvahdjukaar.supplementaries.compat.quark;


import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

public class QuarkPistonPlugin {
    //not really a plugin
    @Nullable
    public static TileEntity getMovingTile(BlockPos pos, World world){
        return PistonsMoveTileEntitiesModule.getMovement(world,pos);
    }

    public static boolean updateMovingTIle(BlockPos pos, World world, TileEntity tile) {
        //not very nice of me to change its private fields :/
        try {
            //Class c = Class.forName("vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule");
            Class c = PistonsMoveTileEntitiesModule.class;
            Field f = ObfuscationReflectionHelper.findField(c,"movements");
            Object o = f.get(null);
            if(o instanceof WeakHashMap){
                WeakHashMap<World, Map<BlockPos, CompoundNBT>> movements = (WeakHashMap<World, Map<BlockPos, CompoundNBT>>) o;
                if (movements.containsKey(world)) {
                    Map<BlockPos, CompoundNBT> worldMovements = movements.get(world);
                    if (worldMovements.containsKey(pos)) {
                        worldMovements.remove(pos);
                        worldMovements.put(pos,tile.serializeNBT());
                        return true;
                    }
                }
            }
        }
        catch (Exception ignored) {}
        return false;
    }

    public static boolean canMoveTile(BlockState state){
        return PistonsMoveTileEntitiesModule.shouldMoveTE(true, state);
    }
}
