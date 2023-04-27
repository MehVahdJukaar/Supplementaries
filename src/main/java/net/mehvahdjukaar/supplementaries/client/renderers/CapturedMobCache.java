package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CapturedMobCache {

    public static LoadingCache<UUID, Entity> MOB_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public Entity load(UUID key) {
                    return null;
                }
            });

    public static void addMob(Entity e) {
        if (e == null) e = getPig();
        MOB_CACHE.put(e.getUUID(), e);
    }

    private static EndCrystal pedestalCrystal = null;

    public static EndCrystal getCrystal(){
        if(pedestalCrystal == null) {
            pedestalCrystal = new EndCrystal(EntityType.END_CRYSTAL, Minecraft.getInstance().level);
            pedestalCrystal.setShowBottom(false);
        }
        return pedestalCrystal;
    }

    private static Entity defaultPig = null;

    @NotNull
    public static Entity getPig(){
        if(defaultPig == null){
            defaultPig = new Pig(EntityType.PIG, Minecraft.getInstance().level);
        }
        return defaultPig;
    }

    @Nullable
    public static Entity getOrCreateCachedMob(UUID id, CompoundTag tag) {
        Entity e = MOB_CACHE.getIfPresent(id);
        if (e == null) {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                CompoundTag mobData = tag.getCompound("EntityData");

                e = MobContainer.createEntityFromNBT(mobData, id, world);
                addMob(e);
            }
        }
        return e;
    }

    public static void tickCrystal() {
        if(pedestalCrystal != null){
            pedestalCrystal.time++;
        }
    }

    public static void unloadLevel(){
        pedestalCrystal = null;
        defaultPig = null;
    }
}
