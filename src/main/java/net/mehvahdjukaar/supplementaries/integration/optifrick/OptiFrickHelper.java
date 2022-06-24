package net.mehvahdjukaar.supplementaries.integration.optifrick;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class OptiFrickHelper {

    private static final Field spawnPos;
    private static final Field spawnBiome;
    private static final boolean optifineInstalled;

    static{
        spawnPos = ObfuscationReflectionHelper.findField(SynchedEntityData.class, "spawnPosition");
        spawnBiome = ObfuscationReflectionHelper.findField(SynchedEntityData.class, "spawnBiome");
        Supplementaries.LOGGER.log(org.apache.logging.log4j.Level.WARN, "aaa-"+spawnPos +"--"+spawnBiome);
        optifineInstalled = (spawnPos != null);
    }

    @Nullable
    public static BlockPos getSpawnPosition(Entity entity){
        if(spawnPos != null){
            try {
                var b = (BlockPos)spawnPos.get(entity.getEntityData());
                Supplementaries.LOGGER.log(org.apache.logging.log4j.Level.WARN,"spawnPos is: "+ b);
                return b;
            } catch (Exception e) {

                Supplementaries.LOGGER.log(org.apache.logging.log4j.Level.ERROR,"getSpawnPosError: "+ e);
            }
        }
        Supplementaries.LOGGER.log(org.apache.logging.log4j.Level.ERROR,"optifineNotInstalled");
        return null;
    }

    public static void initRandomTextures(Entity entity, BlockPos pos, Level level) {
        if(spawnPos != null && spawnBiome != null && level != null && pos !=null){
            try {
                spawnPos.set(entity.getEntityData(), pos);
                spawnBiome.set(entity.getEntityData(), level.getBiome(pos));
            } catch (Exception e) {
                Supplementaries.LOGGER.log(org.apache.logging.log4j.Level.ERROR,"initRandomTextures: "+ e);
            }
        }
    }
}
